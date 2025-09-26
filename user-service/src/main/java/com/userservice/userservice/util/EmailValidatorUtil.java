package com.userservice.userservice.util;



import javax.naming.directory.*;
import javax.naming.*;
import java.util.*;

public class EmailValidatorUtil {

    // ✅ Regex syntax check
    public static boolean isValidEmailFormat(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(regex);
    }

    // ✅ DNS MX Record Check
    public static boolean hasMXRecord(String domain) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            DirContext ictx = new InitialDirContext(env);
            Attributes attrs = ictx.getAttributes(domain, new String[]{"MX"});
            return attrs != null && attrs.get("MX") != null;
        } catch (NamingException e) {
            return false;
        }
    }

    // ✅ Disposable domain list
    private static final Set<String> disposableDomains = new HashSet<>();
    static {
        disposableDomains.add("mailinator.com");
        disposableDomains.add("10minutemail.com");
        disposableDomains.add("tempmail.com");
        disposableDomains.add("guerrillamail.com");
        // Add more as needed
    }

    public static boolean isDisposableEmail(String domain) {
        return disposableDomains.contains(domain.toLowerCase());
    }

    // ✅ Final validation method
    public static boolean isEmailValid(String email) {
        if (!isValidEmailFormat(email)) return false;

        String domain = email.substring(email.indexOf('@') + 1);
        if (isDisposableEmail(domain)) return false;

        return hasMXRecord(domain);
    }
}
