package com.isroot.stash.plugin;

import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;

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
    public YaccPerson(@Nonnull String name, @Nonnull String emailAddress) {
        this.name = name;
        this.emailAddress = emailAddress;
    }

    /**
     * Return the name associated with this identity.
     *
     * @return Name of person.
     */
    @Nonnull
    public String getName() {
        return removeGitCRUD(name);
    }

    /**
     * Return the e-mail address associated with this identity.
     *
     * @return E-mail of person.
     */
    @Nonnull
    public String getEmailAddress() {
        return emailAddress;
    }

    /** Name of person. */
    private final String name;

    /** E-mail address of person */
    private final String emailAddress;


    /** Removes "Illegal Terminating" characters from Git UserNames.  See the GIT Function  static int crud(unsigned char c)*/
    private String removeGitCRUD(@Nonnull String name) {
        return StringUtils.stripEnd(name, ".,:<>\"\\\'");
    }
}
