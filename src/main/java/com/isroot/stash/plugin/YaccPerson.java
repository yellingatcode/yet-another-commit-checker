package com.isroot.stash.plugin;

/**
 * A git person identity.
 */
public class YaccPerson {
    /**
     * Construct a new person value.
     *
     * @param name The name of the person.
     * @param emailAddress The e-mail address of the person.
     */
    public YaccPerson(String name, String emailAddress) {
        this.name = name;
        this.emailAddress = emailAddress;
    }

    /**
     * Return the name associated with this identity.
     *
     * @return Name of person.
     */
    public String getName() {
        return name;
    }

    /**
     * Return the e-mail address associated with this identity.
     *
     * @return E-mail of person.
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /** Name of person. */
    private final String name;

    /** E-mail address of person */
    private final String emailAddress;

}
