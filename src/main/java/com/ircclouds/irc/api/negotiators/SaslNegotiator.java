package com.ircclouds.irc.api.negotiators;


import com.ircclouds.irc.api.*;
import com.ircclouds.irc.api.commands.*;
import com.ircclouds.irc.api.domain.messages.ServerNumericMessage;
import com.ircclouds.irc.api.domain.messages.interfaces.*;
import com.ircclouds.irc.api.listeners.VariousMessageListenerAdapter;
import com.ircclouds.irc.api.negotiators.api.Relay;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of a SASL negotiator. This negotiator will negotiate for the
 * 'sasl' extension. If the extension is acknowledged by the IRC server, it will
 * start the authentication procedure and authenticate the user with the
 * credentials provided in the constructor.
 *
 * @author Danny van Heumen
 */
public class SaslNegotiator extends VariousMessageListenerAdapter implements CapabilityNegotiator {
    private static final Logger LOG = LoggerFactory.getLogger(SaslNegotiator.class);

    private static final String SASL_CAPABILITY_ID = "sasl";

    private static final Pattern AUTHENTICATE_CONFIRMATION = Pattern.compile("AUTHENTICATE\\s+(\\+)\\s*$", 0);

    // AUTHENTICATE numeric replies
    private static final int RPL_LOGGEDIN = 900;
    private static final int RPL_LOGGEDOUT = 901;
    private static final int ERR_NICKLOCKED = 902;
    private static final int RPL_SASLSUCCESS = 903;
    private static final int ERR_SASLFAIL = 904;
    private static final int ERR_SASLTOOLONG = 905;
    private static final int ERR_SASLABORTED = 906;
    private static final int ERR_SASLALREADY = 907;
    private static final int RPL_SASLMECHS = 908;

    private final String user;
    private final String pass;
    private final String authzid;

    private SaslContext state;
    private IRCApi irc;

    // TODO How to handle time-outs? (though not crucial since IRC server will also time-out)
    public SaslNegotiator(final String user, final String pass, final String authzid) {
        this.user = requireNonNull(user);
        this.pass = requireNonNull(pass);
        this.authzid = authzid;
    }

    @Override
    public CapCmd initiate(final IRCApi irc) {
        if (irc == null) {
            throw new IllegalArgumentException("irc instance is required");
        }
        this.irc = irc;
        this.state = new SaslContext(new Relay() {

            @Override
            public void send(final String msg) throws IOException {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("CLIENT: {}", msg);
                }
                SaslNegotiator.this.irc.rawMessage(msg);
            }
        });
        return new CapReqCmd(SASL_CAPABILITY_ID);
    }

    @Override
    public void onMessage(IMessage msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("SERVER: {}", msg.asRaw());
        }
        final String rawmsg = msg.asRaw();
        final Matcher capAck = CAPABILITY_ACK.matcher(rawmsg);
        final Matcher capNak = CAPABILITY_NAK.matcher(rawmsg);
        final Matcher confirmation = AUTHENTICATE_CONFIRMATION.matcher(rawmsg);
        try {
            if (capAck.find() && saslAcknowledged(capAck.group(2))) {
                this.state.init();
            } else if (capNak.find()) {
                this.irc.rawMessage(new CapEndCmd().asString());
            } else if (confirmation.find()) {
                this.state.confirm(confirmation.group(1), this.authzid, this.user, this.pass);
            } else {
                // IGNORING, currently ...
            }
        } catch (final RuntimeException e) {
            LOG.error("Error occurred during CAP negotiation. Prematurely ending CAP negotiation phase and continuing IRC registration as is.", e);
            try {
                this.irc.rawMessage(new CapEndCmd().asString());
            } catch (final IOException ex) {
                throw new IllegalStateException("Failed to gracefully end CAP negotiation.", ex);
            }
        } catch (final IOException e) {
            LOG.error("CAP negotiation interrupted by I/O exception.", e);
            throw new IllegalStateException("SASL negotiation interrupted.", e);
        }
    }

    private boolean saslAcknowledged(final String acknowledged) {
        final String[] caps = acknowledged.split("\\s+");
        for (String cap : caps) {
            if (SASL_CAPABILITY_ID.equals(cap)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onServerNumericMessage(ServerNumericMessage msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("SERVER: {}", msg.asRaw());
        }
        try {
            switch (msg.getNumericCode()) {
                case RPL_LOGGEDIN:
                    this.state.loggedIn();
                    break;
                case RPL_SASLSUCCESS:
                    this.state.success();
                    this.irc.rawMessage(new CapEndCmd().asString());
                    break;
                case ERR_SASLFAIL:
                    this.state.fail();
                    break;
                case ERR_NICKLOCKED:
                    LOG.error("SASL account locked. Aborting authentication procedure.");
                    this.state.abort();
                    this.irc.rawMessage(new CapEndCmd().asString());
                    break;
                case ERR_SASLTOOLONG:
                    this.state.abort();
                    break;
                case ERR_SASLABORTED:
                case ERR_SASLALREADY:
                    this.irc.rawMessage(new CapEndCmd().asString());
                    break;
                default:
                    break;
            }
        } catch (final RuntimeException e) {
            LOG.error("Error occurred during CAP negotiation. Ending CAP negotiation prematurely.", e);
            try {
                this.irc.rawMessage(new CapEndCmd().asString());
            } catch (final IOException ex) {
                throw new IllegalStateException("Failed to gracefully end CAP negotiation.", ex);
            }
        } catch (final IOException e) {
            LOG.error("I/O error occurred during SASL negotiation.", e);
            throw new IllegalStateException("SASL negotiation interrupted by I/O exception.", e);
        }
    }
}
