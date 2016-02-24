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

package org.geppetto.persistence.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.geppetto.core.beans.PathConfiguration;
import org.geppetto.core.data.FindLocalProjectsVisitor;
import org.geppetto.core.data.model.IGeppettoProject;
import org.geppetto.core.data.model.UserPrivileges;
import org.geppetto.persistence.db.DBManager;
import org.geppetto.persistence.db.model.GeppettoProject;
import org.geppetto.persistence.db.model.User;
import org.geppetto.persistence.db.model.UserGroup;

public class DBOSBData
{

	private DBManager dbManager;
	private User user;

	private final static String osbPath = "/home/adrian/Programs/virgo-tomcat-server-3.6.3.RELEASE/OSB_Samples/";

	public DBOSBData()
	{
		dbManager = new DBManager();
		dbManager.setPersistenceManagerFactory(getPersistenceManagerFactory());

	}

	public static PersistenceManagerFactory getPersistenceManagerFactory()
	{
		Map<String, String> dbConnProperties = new HashMap<>();
		dbConnProperties.put("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
		dbConnProperties.put("datanucleus.storeManagerType", "rdbms");
		dbConnProperties.put("datanucleus.connection.resourceType", "RESOURCE_LOCAL");
		dbConnProperties.put("datanucleus.DetachAllOnCommit", "true");
		dbConnProperties.put("datanucleus.validateTables", "true");
		dbConnProperties.put("datanucleus.connection2.resourceType", "RESOURCE_LOCAL");
		dbConnProperties.put("datanucleus.autoCreateSchema", "true");
		dbConnProperties.put("datanucleus.autoCreateColumns", "true");
		File dbConnFile = new File(PathConfiguration.settingsFolder + "/db.properties");
		try
		{
			List<String> lines = Files.readAllLines(dbConnFile.toPath(), Charset.defaultCharset());
			for(String line : lines)
			{
				int eqIndex = line.indexOf("=");
				if(!line.startsWith("#") && eqIndex > 0)
				{
					dbConnProperties.put(line.substring(0, eqIndex), line.substring(eqIndex + 1));
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		return JDOHelper.getPersistenceManagerFactory(dbConnProperties);
	}

	private void buildSuperGroupAndPrivileges()
	{
		// Anonymous group & permission
		long value = 1000l * 1000 * 1000;
		List<UserPrivileges> privileges2 = new ArrayList<UserPrivileges>();
		privileges2.add(UserPrivileges.READ_PROJECT);
		privileges2.add(UserPrivileges.WRITE_PROJECT);
		privileges2.add(UserPrivileges.DOWNLOAD);
		privileges2.add(UserPrivileges.DROPBOX_INTEGRATION);
		privileges2.add(UserPrivileges.RUN_EXPERIMENT);
		UserGroup supergroup = new UserGroup("supergroup", privileges2, value, value * 2);
		dbManager.storeEntity(supergroup);
	}

	private void buildAnonymousUserGroupAndPrivileges()
	{
		// User group & permission
		List<UserPrivileges> privileges = new ArrayList<UserPrivileges>();
		privileges.add(UserPrivileges.READ_PROJECT);
		privileges.add(UserPrivileges.DOWNLOAD);
		UserGroup group = new UserGroup("anonymousGroup", privileges, 0, 0);
		user = new User("osbanonymous", "anonymous", "osbanonymous", new ArrayList<GeppettoProject>(), group);
		dbManager.storeEntity(group);
		dbManager.storeEntity(user);
	}

	private void buildLocalProjects() throws URISyntaxException, IOException
	{
		Map<Long, GeppettoProject> localProjects = new ConcurrentHashMap<Long, GeppettoProject>();
		FindLocalProjectsVisitor<GeppettoProject> findProjectsVisitor = new FindLocalProjectsVisitor<GeppettoProject>(localProjects, GeppettoProject.class);
		Files.walkFileTree(Paths.get(osbPath), findProjectsVisitor);

		user = dbManager.findUserByLogin("osbanonymous");
		List<GeppettoProject> userProjects = user.getGeppettoProjects();
		for (IGeppettoProject localProject : localProjects.values()){
			userProjects.add((GeppettoProject)localProject);
		}
		dbManager.storeEntity(user);
	}

	public static void main(String[] args) throws URISyntaxException, IOException
	{
		DBOSBData dbOSBCreator = new DBOSBData();
		dbOSBCreator.buildSuperGroupAndPrivileges();
		dbOSBCreator.buildAnonymousUserGroupAndPrivileges();
		dbOSBCreator.buildLocalProjects();
	}

}
