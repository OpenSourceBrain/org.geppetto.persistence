/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2011 - 2015 OpenWorm.
 * http://openworm.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/MIT
 *
 * Contributors:
 *     	OpenWorm - http://openworm.org/people.html
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE 
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/

package org.geppetto.persistence.auth;

import org.geppetto.core.auth.IAuthService;
import org.geppetto.core.data.model.IUser;
import org.springframework.stereotype.Service;

@Service
public class GeppettoAuthentication implements IAuthService
{

	@Override
	public String authFailureRedirect()
	{
		return "http://geppetto.org";
	}

	@Override
	public boolean isDefault()
	{
		return false;
	}

	@Override
	public Boolean isAuthenticated(String sessionValue)
	{
		// Authentication is checked in the frontend with shiro
		return false;
	}

	@Override
	public String getSessionId()
	{
		return null;
	}

	@Override
	public void setUser(IUser user)
	{
		// TODO User is stored at the moment in the GeppettoManager, it would make more sense to probably have it here
	}

	@Override
	public IUser getUser()
	{
		// TODO User is stored at the moment in the GeppettoManager, it would make more sense to probably have it here
		return null;
	}

}
