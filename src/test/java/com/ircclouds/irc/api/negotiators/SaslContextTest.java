package com.ircclouds.irc.api.negotiators;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ircclouds.irc.api.negotiators.api.Relay;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Danny van Heumen
 */
public class SaslContextTest {
    @Test(expected = NullPointerException.class)
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testConstructionNullRelay() {
        new SaslContext(null);
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testConstruction() {
        Relay relay = mock(Relay.class);
        new SaslContext(relay);
    }

    @Test(expected = IllegalStateException.class)
    public void testNoInitiateConfirm() throws IOException {
        Relay relay = mock(Relay.class);
        SaslContext context = new SaslContext(relay);
        context.confirm("+", "user", "pass", "role");
    }

    @Test(expected = IllegalStateException.class)
    public void testNoInitiateLoggedIn() {
        Relay relay = mock(Relay.class);
        SaslContext context = new SaslContext(relay);
        context.loggedIn();
    }

    @Test(expected = IllegalStateException.class)
    public void testNoInitiateSuccess() {
        Relay relay = mock(Relay.class);
        SaslContext context = new SaslContext(relay);
        context.success();
    }

    @Test
    public void testInit() throws IOException {
        Relay relay = mock(Relay.class);
        SaslContext context = new SaslContext(relay);
        context.init();
        verify(relay).send("AUTHENTICATE PLAIN");
    }

    @Test(expected = IllegalStateException.class)
    public void testNoConfirmLoggedIn() throws IOException {
        Relay relay = mock(Relay.class);
        SaslContext context = new SaslContext(relay);
        context.init();
        context.loggedIn();
    }

    @Test(expected = IllegalStateException.class)
    public void testNoConfirmSuccess() throws IOException {
        Relay relay = mock(Relay.class);
        SaslContext context = new SaslContext(relay);
        context.init();
        context.success();
    }

    @Test
    public void testConfirm() throws IOException {
        Relay relay = mock(Relay.class);
        SaslContext context = new SaslContext(relay);
        context.init();
        context.confirm("+", "jilles", "jilles", "sesame");
        verify(relay).send("AUTHENTICATE PLAIN");
        verify(relay).send("AUTHENTICATE amlsbGVzAGppbGxlcwBzZXNhbWU=");
    }

    @Test
    public void testConfirmWithMessageCutoff() throws IOException {
        Relay relay = mock(Relay.class);
        SaslContext context = new SaslContext(relay);
        context.init();
        context.confirm("+", "jillesjillesjillesjillesjillesjillesjillesjillesjillesjillesjillesjillesjillesjillesjillesjillesjillesjillesjillesjilles", "jillesjillesjillesjillesjillesjillesjillesjillesjillesjilles", "sesamesesamesesamesesamesesamesesamesesamesesamesesamesesamesesamesesamesesamesesamesesamesesamesesamesesamesesamesesame");
        verify(relay).send("AUTHENTICATE PLAIN");
        verify(relay).send("AUTHENTICATE amlsbGVzamlsbGVzamlsbGVzamlsbGVzamlsbGVzamlsbGVzamlsbGVzamlsbGVzamlsbGVzamlsbGVzamlsbGVzamlsbGVzamlsbGVzamlsbGVzamlsbGVzamlsbGVzamlsbGVzamlsbGVzamlsbGVzamlsbGVzAGppbGxlc2ppbGxlc2ppbGxlc2ppbGxlc2ppbGxlc2ppbGxlc2ppbGxlc2ppbGxlc2ppbGxlc2ppbGxlcwBzZXNhbWVzZXNhbWVzZXNhbWVzZXNhbWVzZXNhbWVzZXNhbWVzZXNhbWVzZXNhbWVzZXNhbWVzZXNhbWVzZXNhbWVzZXNhbWVzZXNhbWVzZXNhbWVzZXNhbWVzZXNhbWVzZXNhbWVzZXNhbWV");
    }

    @Test
    public void testConfirmLoggedIn() throws IOException {
        Relay relay = mock(Relay.class);
        SaslContext context = new SaslContext(relay);
        context.init();
        context.confirm("+", "jilles", "jilles", "sesame");
        context.loggedIn();
        verify(relay).send("AUTHENTICATE PLAIN");
        verify(relay).send("AUTHENTICATE amlsbGVzAGppbGxlcwBzZXNhbWU=");
    }

    @Test
    public void testConfirmSuccess() throws IOException {
        Relay relay = mock(Relay.class);
        SaslContext context = new SaslContext(relay);
        context.init();
        context.confirm("+", "jilles", "jilles", "sesame");
        context.success();
        verify(relay).send("AUTHENTICATE PLAIN");
        verify(relay).send("AUTHENTICATE amlsbGVzAGppbGxlcwBzZXNhbWU=");
    }

    @Test
    public void testFullyAuthenticated() throws IOException {
        Relay relay = mock(Relay.class);
        SaslContext context = new SaslContext(relay);
        context.init();
        context.confirm("+", "jilles", "jilles", "sesame");
        context.loggedIn();
        context.success();
        verify(relay).send("AUTHENTICATE PLAIN");
        verify(relay).send("AUTHENTICATE amlsbGVzAGppbGxlcwBzZXNhbWU=");
    }

    @Test(expected = IllegalStateException.class)
    public void testFailBeforeInitiation() throws IOException {
        Relay relay = mock(Relay.class);
        SaslContext context = new SaslContext(relay);
        context.fail();
    }

    @Test(expected = IllegalStateException.class)
    public void testAbortBeforeInitiation() throws IOException {
        Relay relay = mock(Relay.class);
        SaslContext context = new SaslContext(relay);
        context.abort();
    }

    @Test
    public void testAbortAfterInitiation() throws IOException {
        Relay relay = mock(Relay.class);
        SaslContext context = new SaslContext(relay);
        context.init();
        context.abort();
        verify(relay).send("AUTHENTICATE PLAIN");
        verify(relay).send("AUTHENTICATE *");
    }

    @Test
    public void testAbortAfterConfirmation() throws IOException {
        Relay relay = mock(Relay.class);
        SaslContext context = new SaslContext(relay);
        context.init();
        context.confirm("+", "jilles", "jilles", "sesame");
        context.abort();
        verify(relay).send("AUTHENTICATE PLAIN");
        verify(relay).send("AUTHENTICATE amlsbGVzAGppbGxlcwBzZXNhbWU=");
        verify(relay).send("AUTHENTICATE *");
    }

    @Test
    public void testFailAfterSuccessfulAuthentication() throws IOException {
        Relay relay = mock(Relay.class);
        SaslContext context = new SaslContext(relay);
        context.init();
        context.confirm("+", "jilles", "jilles", "sesame");
        context.loggedIn();
        context.success();
        context.fail();
        verify(relay).send("AUTHENTICATE PLAIN");
        verify(relay).send("AUTHENTICATE amlsbGVzAGppbGxlcwBzZXNhbWU=");
    }

    @Test
    public void testAbortOnFailedAuthenticationMechanism() throws IOException {
        Relay relay = mock(Relay.class);
        SaslContext context = new SaslContext(relay);
        context.init();
        context.fail();
        verify(relay).send("AUTHENTICATE PLAIN");
        verify(relay).send("AUTHENTICATE *");
    }

    @Test
    public void testAbortOnFailedAuthentication() throws IOException {
        Relay relay = mock(Relay.class);
        SaslContext context = new SaslContext(relay);
        context.init();
        context.confirm("+", "jilles", "jilles", "sesame");
        context.fail();
        verify(relay).send("AUTHENTICATE PLAIN");
        verify(relay).send("AUTHENTICATE amlsbGVzAGppbGxlcwBzZXNhbWU=");
        verify(relay).send("AUTHENTICATE *");
    }
}
