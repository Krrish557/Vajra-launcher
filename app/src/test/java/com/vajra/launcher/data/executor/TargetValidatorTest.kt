package com.vajra.launcher.data.executor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TargetValidatorTest {

    @Test
    fun testValidIpv4Targets() {
        assertTrue(TargetValidator.isValid("127.0.0.1"))
        assertTrue(TargetValidator.isValid("192.168.1.1"))
        assertTrue(TargetValidator.isValid("10.0.0.1"))
        assertTrue(TargetValidator.isValid("172.16.254.1"))
        assertTrue(TargetValidator.isValid("255.255.255.255"))
        assertTrue(TargetValidator.isValid("0.0.0.0"))
    }

    @Test
    fun testValidIpv4CidrTargets() {
        assertTrue(TargetValidator.isValid("192.168.1.0/24"))
        assertTrue(TargetValidator.isValid("10.0.0.0/8"))
        assertTrue(TargetValidator.isValid("172.16.0.0/16"))
        assertTrue(TargetValidator.isValid("127.0.0.1/32"))
        assertTrue(TargetValidator.isValid("0.0.0.0/0"))
    }

    @Test
    fun testValidHostnames() {
        assertTrue(TargetValidator.isValid("localhost"))
        assertTrue(TargetValidator.isValid("scanme.nmap.org"))
        assertTrue(TargetValidator.isValid("example.com"))
        assertTrue(TargetValidator.isValid("internal-api.sub.domain.co"))
        assertTrue(TargetValidator.isValid("router"))
    }

    @Test
    fun testValidIpv6Targets() {
        assertTrue(TargetValidator.isValid("::1"))
        assertTrue(TargetValidator.isValid("2001:db8::1"))
        assertTrue(TargetValidator.isValid("fe80::1"))
        assertTrue(TargetValidator.isValid("fe80::1/64"))
        assertTrue(TargetValidator.isValid("2001:0db8:85a3:0000:0000:8a2e:0370:7334"))
    }

    @Test
    fun testRejectsCommandInjectionShellMetacharacters() {
        // Semicolon
        assertFalse(TargetValidator.isValid("127.0.0.1;whoami"))
        assertFalse(TargetValidator.isValid(";whoami"))
        assertFalse(TargetValidator.isValid("127.0.0.1;"))

        // Logical AND / OR
        assertFalse(TargetValidator.isValid("127.0.0.1 && whoami"))
        assertFalse(TargetValidator.isValid("127.0.0.1&&whoami"))
        assertFalse(TargetValidator.isValid("127.0.0.1 || whoami"))
        assertFalse(TargetValidator.isValid("127.0.0.1||whoami"))

        // Pipes
        assertFalse(TargetValidator.isValid("127.0.0.1 | whoami"))
        assertFalse(TargetValidator.isValid("127.0.0.1|whoami"))

        // Command substitution: $() and backticks
        assertFalse(TargetValidator.isValid("$(whoami)"))
        assertFalse(TargetValidator.isValid("127.0.0.1$(whoami)"))
        assertFalse(TargetValidator.isValid("`whoami`"))
        assertFalse(TargetValidator.isValid("127.0.0.1`whoami`"))

        // Redirection: < and >
        assertFalse(TargetValidator.isValid("127.0.0.1 > file"))
        assertFalse(TargetValidator.isValid("127.0.0.1>file"))
        assertFalse(TargetValidator.isValid("127.0.0.1 < file"))
        assertFalse(TargetValidator.isValid("127.0.0.1<file"))
        assertFalse(TargetValidator.isValid("127.0.0.1 >> /etc/passwd"))

        // Whitespace and control characters
        assertFalse(TargetValidator.isValid("127.0.0.1 whoami"))
        assertFalse(TargetValidator.isValid("127.0.0.1\nwhoami"))
        assertFalse(TargetValidator.isValid("127.0.0.1\rwhoami"))
        assertFalse(TargetValidator.isValid("127.0.0.1\twhoami"))
        assertFalse(TargetValidator.isValid("127.0.0.1\u0000whoami"))

        // Quotes, brackets, wildcards
        assertFalse(TargetValidator.isValid("127.0.0.1'"))
        assertFalse(TargetValidator.isValid("127.0.0.1\""))
        assertFalse(TargetValidator.isValid("127.0.0.1!"))
        assertFalse(TargetValidator.isValid("127.0.0.1*"))
        assertFalse(TargetValidator.isValid("127.0.0.1?"))
        assertFalse(TargetValidator.isValid("127.0.0.1{whoami}"))
        assertFalse(TargetValidator.isValid("127.0.0.1[whoami]"))
        assertFalse(TargetValidator.isValid("127.0.0.1#comment"))
        assertFalse(TargetValidator.isValid("~"))
    }

    @Test
    fun testRejectsInvalidFormats() {
        assertFalse(TargetValidator.isValid(""))
        assertFalse(TargetValidator.isValid("   "))
        assertFalse(TargetValidator.isValid("256.0.0.1"))
        assertFalse(TargetValidator.isValid("192.168.1.1.1"))
        assertFalse(TargetValidator.isValid("192.168.1.0/33"))
        assertFalse(TargetValidator.isValid(":::1"))
        assertFalse(TargetValidator.isValid("foo:bar"))
        assertFalse(TargetValidator.isValid("-invalid.com"))
        assertFalse(TargetValidator.isValid("invalid.com-"))
    }
}
