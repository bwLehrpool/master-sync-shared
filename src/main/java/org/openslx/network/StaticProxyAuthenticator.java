package org.openslx.network;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class StaticProxyAuthenticator extends Authenticator
{
	private final String username, password;

	public StaticProxyAuthenticator( String username, String password )
	{
		this.username = username;
		this.password = password;
	}

	protected PasswordAuthentication getPasswordAuthentication()
	{
		return new PasswordAuthentication(
				this.username, this.password.toCharArray() );
	}
}
