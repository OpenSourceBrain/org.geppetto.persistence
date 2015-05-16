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

package org.geppetto.persistence.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.FetchGroup;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.data.model.ExperimentStatus;
import org.geppetto.core.data.model.PersistedDataType;
import org.geppetto.persistence.db.model.AspectConfiguration;
import org.geppetto.persistence.db.model.Experiment;
import org.geppetto.persistence.db.model.GeppettoProject;
import org.geppetto.persistence.db.model.InstancePath;
import org.geppetto.persistence.db.model.Parameter;
import org.geppetto.persistence.db.model.PersistedData;
import org.geppetto.persistence.db.model.SimulationResult;
import org.geppetto.persistence.db.model.SimulatorConfiguration;
import org.geppetto.persistence.db.model.User;

public class DBManager
{

	private PersistenceManagerFactory pmf;

	private static Log _logger = LogFactory.getLog(DBManager.class);

	public DBManager()
	{
		// TODO: this will be removed once we have real DB usage
		new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					Thread.sleep(5000);
				}
				catch(InterruptedException e)
				{
					// ignore
				}
				// doSomeRealModelDBWork();
			}
		}).start();
	}

	public void setPersistenceManagerFactory(PersistenceManagerFactory pmf)
	{
		this.pmf = pmf;
	}

	public <T> void storeEntity(T entity)
	{
		PersistenceManager pm = pmf.getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try
		{
			tx.begin();
			pm.makePersistent(entity);
			tx.commit();
		}
		catch(Exception e)
		{
			_logger.warn("Could not store data", e);
		}
		finally
		{
			if(tx.isActive())
			{
				tx.rollback();
			}
			pm.close();
		}
	}

	public <T> List<T> getAllEntities(Class<T> type)
	{
		PersistenceManager pm = pmf.getPersistenceManager();
		try
		{
			pm.getFetchPlan().setGroup(FetchGroup.ALL);
			pm.getFetchPlan().setFetchSize(FetchPlan.FETCH_SIZE_GREEDY);
			pm.getFetchPlan().setMaxFetchDepth(5);
			Query query = pm.newQuery(type);
			return (List<T>) query.execute();
		}
		finally
		{
			pm.close();
		}
	}

	public <T> void deleteAllEntities(Class<T> type)
	{
		PersistenceManager pm = pmf.getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try
		{
			tx.begin();
			Query query = pm.newQuery(type);
			List<T> entities = (List<T>) query.execute();
			pm.deletePersistentAll(entities);
			tx.commit();
		}
		catch(Exception e)
		{
			_logger.warn("Could not delete data", e);
		}
		finally
		{
			if(tx.isActive())
			{
				tx.rollback();
			}
			pm.close();
		}
	}

	public <T> void deleteEntity(T entity)
	{
		PersistenceManager pm = pmf.getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try
		{
			tx.begin();
			pm.deletePersistent(entity);
			tx.commit();
		}
		catch(Exception e)
		{
			_logger.warn("Could not delete data", e);
		}
		finally
		{
			if(tx.isActive())
			{
				tx.rollback();
			}
			pm.close();
		}
	}

	public <T> T findEntityById(Class<T> type, long id)
	{
		PersistenceManager pm = pmf.getPersistenceManager();
		try
		{
			pm.getFetchPlan().setGroup(FetchGroup.ALL);
			pm.getFetchPlan().setFetchSize(FetchPlan.FETCH_SIZE_GREEDY);
			pm.getFetchPlan().setMaxFetchDepth(5);
			Query query = pm.newQuery(type);
			query.setFilter("id == searchedId");
			query.declareParameters("int searchedId");
			List<T> entities = (List<T>) query.execute(id);
			if(entities.size() > 0)
			{
				return entities.get(0);
			}
			return null;
		}
		finally
		{
			pm.close();
		}
	}

	public User findUserByLogin(String login)
	{
		PersistenceManager pm = pmf.getPersistenceManager();
		try
		{
			pm.getFetchPlan().setGroup(FetchGroup.ALL);
			pm.getFetchPlan().setFetchSize(FetchPlan.FETCH_SIZE_GREEDY);
			pm.getFetchPlan().setMaxFetchDepth(5);
			Query query = pm.newQuery(User.class);
			query.setFilter("login == searchedLogin");
			query.declareParameters("String searchedLogin");
			List<User> users = (List<User>) query.execute(login);
			if(users.size() > 0)
			{
				return users.get(0);
			}
			return null;
		}
		finally
		{
			pm.close();
		}
	}

	private void doSomeRealModelDBWork()
	{
		List<User> users = getAllEntities(User.class);
		deleteAllEntities(User.class);
		deleteAllEntities(GeppettoProject.class);
		deleteAllEntities(Experiment.class);
		deleteAllEntities(Parameter.class);
		deleteAllEntities(PersistedData.class);
		deleteAllEntities(AspectConfiguration.class);
		deleteAllEntities(SimulationResult.class);
		deleteAllEntities(SimulatorConfiguration.class);
		deleteAllEntities(InstancePath.class);
		users = getAllEntities(User.class);

		long suffix = System.currentTimeMillis() % 1000;
		PersistedData persistedData = new PersistedData("some url", PersistedDataType.GEPPETTO_PROJECT);
		InstancePath instancePath = new InstancePath("entityInstancePath", "aspect", "localInstancePath");
		InstancePath instancePath2 = new InstancePath("entityInstancePath2", "aspect2", "localInstancePath2");
		List<InstancePath> instancePaths = new ArrayList<>();
		instancePaths.add(instancePath);
		instancePaths.add(instancePath2);
		List<Parameter> params = new ArrayList<Parameter>();
		Parameter param1 = new Parameter(instancePath, "value " + suffix);
		storeEntity(param1);
		Parameter param2 = new Parameter(instancePath2, "value2 " + suffix);
		params.add(param1);
		params.add(param2);

		InstancePath aspect = new InstancePath("entityInstancePathAspect", "aspectAspect", "localInstancePathAspect");
		Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("key", "value");
		SimulatorConfiguration simulatorConfiguration = new SimulatorConfiguration("simulatorId", "conversionServiceId", 0.1f, parameters);
		List<AspectConfiguration> aspectConfigurations = new ArrayList<>();
		AspectConfiguration aspectConfiguration = new AspectConfiguration(aspect, instancePaths, params, simulatorConfiguration);
		aspectConfigurations.add(aspectConfiguration);

		List<SimulationResult> simulationResults = new ArrayList<>();
		SimulationResult simulationResult = new SimulationResult(aspect, persistedData);
		simulationResults.add(simulationResult);

		List<Experiment> experiments = new ArrayList<Experiment>();
		Experiment experiment = new Experiment(aspectConfigurations, "experiment " + suffix, "experiment description", new Date(), new Date(), ExperimentStatus.COMPLETED, simulationResults,
				new Date(), new Date());
		experiments.add(experiment);
		GeppettoProject project = new GeppettoProject("project " + suffix, experiments, persistedData);
		GeppettoProject project2 = new GeppettoProject("project2 " + suffix, experiments, persistedData);
		GeppettoProject project3 = new GeppettoProject("project3 " + suffix, experiments, persistedData);
		GeppettoProject project4 = new GeppettoProject("project4 " + suffix, experiments, persistedData);
		List<GeppettoProject> projects = new ArrayList<GeppettoProject>();
		projects.add(project);
		projects.add(project2);
		projects.add(project3);
		projects.add(project4);
		long value = 1000l * 1000 * 1000;
		User user = new User("guest", "Guest user", projects, value, 2 * value);
		storeEntity(user);
	}

}
