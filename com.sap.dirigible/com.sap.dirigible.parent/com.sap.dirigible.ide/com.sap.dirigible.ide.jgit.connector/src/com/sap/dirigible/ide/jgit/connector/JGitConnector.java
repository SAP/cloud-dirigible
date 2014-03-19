/*******************************************************************************
 * Copyright (c) 2014 SAP AG or an SAP affiliate company. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *******************************************************************************/

package com.sap.dirigible.ide.jgit.connector;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.RebaseCommand.Operation;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.sap.dirigible.ide.common.CommonParameters;
import com.sap.dirigible.ide.jgit.utils.GitFileUtils;
import com.sap.dirigible.ide.logging.Logger;

public class JGitConnector {
	private static final String REFS_HEADS_MASTER = "refs/heads/master"; //$NON-NLS-1$
	private static final String MERGE = "merge"; //$NON-NLS-1$
	private static final String MASTER = "master"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PROXY_PROPERTIES_FILE_LOCATION = "proxy.properties"; //$NON-NLS-1$
	private static final String DEFAULT_PROXY_VALUE = "false"; //$NON-NLS-1$
	private static final String PROXY = "proxy"; //$NON-NLS-1$
	public static final String HTTP_PROXY_HOST = "http.proxyHost"; //$NON-NLS-1$
	public static final String HTTP_PROXY_PORT = "http.proxyPort"; //$NON-NLS-1$
	public static final String HTTPS_PROXY_HOST = "https.proxyHost"; //$NON-NLS-1$
	public static final String HTTPS_PROXY_PORT = "https.proxyPort"; //$NON-NLS-1$
	public static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts"; //$NON-NLS-1$
	public static final String TEMP_DIRECTORY_PREFIX = "com.sap.dirigible.jgit."; //$NON-NLS-1$
	private static final String ADD_ALL_NEW_FILES = "."; //$NON-NLS-1$

	private static final Logger logger = Logger.getLogger(JGitConnector.class);

	static {
		try {
			loadProxy();
			deleteTempDirectories();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	private static void loadProxy() throws IOException {
		// if (System.getProperty(HTTP_PROXY_HOST) == null) {
		// InputStream in = JGitConnector.class
		// .getResourceAsStream(PROXY_PROPERTIES_FILE_LOCATION);
		// Properties properties = new Properties();
		// properties.load(in);
		//
		// String proxy = properties.getProperty(PROXY, DEFAULT_PROXY_VALUE);
		// boolean needsProxy = Boolean.parseBoolean(proxy);
		//
		// if (needsProxy) {
		// final String httpProxyHost = properties.getProperty(HTTP_PROXY_HOST);
		// final String httpProxyPort = properties.getProperty(HTTP_PROXY_PORT);
		// final String httpsProxyHost =
		// properties.getProperty(HTTPS_PROXY_HOST);
		// final String httpsProxyPort =
		// properties.getProperty(HTTPS_PROXY_PORT);
		//
		// System.setProperty(HTTP_PROXY_HOST, httpProxyHost);
		// System.setProperty(HTTP_PROXY_PORT, httpProxyPort);
		// System.setProperty(HTTPS_PROXY_HOST, httpsProxyHost);
		// System.setProperty(HTTPS_PROXY_PORT, httpsProxyPort);
		// }

		String parameterHTTP_PROXY_HOST = CommonParameters.get(HTTP_PROXY_HOST);
		if (parameterHTTP_PROXY_HOST != null) {
			System.setProperty(HTTP_PROXY_HOST, parameterHTTP_PROXY_HOST);
			logger.debug("HTTP_PROXY_HOST:" + parameterHTTP_PROXY_HOST);
		} else {
			logger.debug("HTTP_PROXY_HOST not set");
		}
		String parameterHTTP_PROXY_PORT = CommonParameters.get(HTTP_PROXY_PORT);
		if (parameterHTTP_PROXY_PORT != null) {
			System.setProperty(HTTP_PROXY_PORT, parameterHTTP_PROXY_PORT);
			logger.debug("HTTP_PROXY_PORT:" + parameterHTTP_PROXY_PORT);
		} else {
			logger.debug("HTTP_PROXY_PORT not set");
		}
		String parameterHTTPS_PROXY_HOST = CommonParameters.get(HTTPS_PROXY_HOST);
		if (parameterHTTPS_PROXY_HOST != null) {
			System.setProperty(HTTPS_PROXY_HOST, parameterHTTPS_PROXY_HOST);
			logger.debug("HTTPS_PROXY_HOST:" + parameterHTTPS_PROXY_HOST);
		} else {
			logger.debug("HTTPS_PROXY_HOST not set");
		}
		String parameterHTTPS_PROXY_PORT = CommonParameters.get(HTTPS_PROXY_PORT);
		if (parameterHTTPS_PROXY_PORT != null) {
			System.setProperty(HTTPS_PROXY_PORT, parameterHTTPS_PROXY_PORT);
			logger.debug("HTTPS_PROXY_PORT:" + parameterHTTPS_PROXY_PORT);
		} else {
			logger.debug("HTTPS_PROXY_PORT not set");
		}
		String parameterHTTP_NON_PROXY_HOSTS = CommonParameters.get(HTTP_NON_PROXY_HOSTS);
		if (parameterHTTP_NON_PROXY_HOSTS != null) {
			System.setProperty(HTTP_NON_PROXY_HOSTS, parameterHTTP_NON_PROXY_HOSTS);
			logger.debug("HTTP_NON_PROXY_HOSTS:" + parameterHTTP_NON_PROXY_HOSTS);
		} else {
			logger.debug("HTTP_NON_PROXY_HOSTS not set");
		}

		try {
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} };
			// Install the all-trusting trust manager
			final SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (KeyManagementException e) {
			throw new IOException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}

		// }
	}

	private static void deleteTempDirectories() throws IOException {
		File file = GitFileUtils.createTempDirectory("DeleteDirectory");
		File tempDirectory = file.getParentFile();
		for (File temp : tempDirectory.listFiles()) {
			if (temp.isDirectory() && temp.getName().startsWith(TEMP_DIRECTORY_PREFIX)) {
				GitFileUtils.deleteDirectory(temp);
			}
		}
		GitFileUtils.deleteDirectory(file);
	}

	/**
	 * 
	 * Gets org.eclipse.jgit.lib.Repository object for existing Git Repository.
	 * 
	 * @param repositoryPath
	 *            the path to an existing Git Repository
	 * @return {@link org.eclipse.jgit.lib.Repository} object
	 * 
	 * 
	 * @throws IOException
	 */
	public static Repository getRepository(String repositoryPath) throws IOException {
		RepositoryBuilder repositoryBuilder = new RepositoryBuilder();
		repositoryBuilder.findGitDir(new File(repositoryPath));
		Repository repository = repositoryBuilder.build();
		repository.getConfig().setString(BRANCH, MASTER, MERGE, REFS_HEADS_MASTER);
		return repository;
	}

	/**
	 * 
	 * Clones Git remote repository to the file system.
	 * 
	 * @param repositoryURI
	 *            repository's URI example: https://qwerty.com/xyz/abc.git
	 * @param gitDirectory
	 *            where the remote repository will be cloned
	 * @throws InvalidRemoteException
	 * @throws TransportException
	 * @throws GitAPIException
	 */
	public static void cloneRepository(String repositoryURI, File gitDirectory)
			throws InvalidRemoteException, TransportException, GitAPIException {
		CloneCommand cloneCommand = Git.cloneRepository();
		cloneCommand.setURI(repositoryURI);
		cloneCommand.setRemote(Constants.DEFAULT_REMOTE_NAME);
		cloneCommand.setDirectory(gitDirectory);
		cloneCommand.call();
	}

	private final Git git;
	private Repository repository;

	public JGitConnector(Repository repository) throws IOException {
		this.repository = repository;
		this.git = new Git(repository);
	}

	/**
	 * 
	 * Adds content from file(s) to the staging index
	 * 
	 * @param filePattern
	 *            File to add content from. Example: "." includes all files. If
	 *            "dir/subdir/" is directory then "dir/subdir" all files from
	 *            the directory recursively
	 * @throws IOException
	 * @throws NoFilepatternException
	 * @throws GitAPIException
	 */
	public void add(String filePattern) throws IOException, NoFilepatternException, GitAPIException {
		AddCommand addCommand = git.add();
		addCommand.addFilepattern(filePattern);
		addCommand.call();
	}

	/**
	 * 
	 * Adds all changes to the staging index. Then makes commit.
	 * 
	 * @param message
	 *            the commit message
	 * @throws NoHeadException
	 * @throws NoMessageException
	 * @throws UnmergedPathsException
	 * @throws ConcurrentRefUpdateException
	 * @throws WrongRepositoryStateException
	 * @throws GitAPIException
	 * @throws IOException
	 */
	public void commit(String message) throws NoHeadException, NoMessageException,
			UnmergedPathsException, ConcurrentRefUpdateException, WrongRepositoryStateException,
			GitAPIException, IOException {
		add(ADD_ALL_NEW_FILES);
		CommitCommand commitCommand = git.commit();
		commitCommand.setMessage(message);
		commitCommand.setAll(true);
		commitCommand.call();
	}

	/**
	 * 
	 * Creates new branch from a particular start point
	 * 
	 * @param name
	 *            the branch name
	 * @param startPoint
	 *            valid tree-ish object example: "5c15e8", "master", "HEAD",
	 *            "21d5a96070353d01c0f30bc0559ab4de4f5e3ca0"
	 * @throws RefAlreadyExistsException
	 * @throws RefNotFoundException
	 * @throws InvalidRefNameException
	 * @throws GitAPIException
	 */
	public void createBranch(String name, String startPoint) throws RefAlreadyExistsException,
			RefNotFoundException, InvalidRefNameException, GitAPIException {
		repository.getConfig().setString(BRANCH, name, MERGE, REFS_HEADS_MASTER);
		CreateBranchCommand createBranchCommand = git.branchCreate();
		createBranchCommand.setName(name);
		createBranchCommand.setStartPoint(startPoint);
		createBranchCommand.setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM);
		createBranchCommand.call();
	}

	/**
	 * 
	 * Checkout to a valid tree-ish object example: "5c15e8", "master", "HEAD",
	 * "21d5a96070353d01c0f30bc0559ab4de4f5e3ca0"
	 * 
	 * @param name
	 *            the tree-ish object
	 * @return {@link org.eclipse.jgit.lib.Ref} object
	 * @throws RefAlreadyExistsException
	 * @throws RefNotFoundException
	 * @throws InvalidRefNameException
	 * @throws CheckoutConflictException
	 * @throws GitAPIException
	 */
	public Ref checkout(String name) throws RefAlreadyExistsException, RefNotFoundException,
			InvalidRefNameException, CheckoutConflictException, GitAPIException {
		CheckoutCommand checkoutCommand = git.checkout();
		checkoutCommand.setName(name);
		return checkoutCommand.call();
	}

	/**
	 * 
	 * Hard reset the repository. Makes the working directory and staging index
	 * content to exactly match the Git repository.
	 * 
	 * @throws CheckoutConflictException
	 * @throws GitAPIException
	 */
	public void hardReset() throws CheckoutConflictException, GitAPIException {
		ResetCommand resetCommand = git.reset();
		resetCommand.setMode(ResetType.HARD);
		resetCommand.call();
	}

	/**
	 * 
	 * Fetches from a remote repository and tries to merge into the current
	 * branch.
	 * 
	 * @throws WrongRepositoryStateException
	 * @throws InvalidConfigurationException
	 * @throws DetachedHeadException
	 * @throws InvalidRemoteException
	 * @throws CanceledException
	 * @throws RefNotFoundException
	 * @throws NoHeadException
	 * @throws TransportException
	 * @throws GitAPIException
	 */
	public void pull() throws WrongRepositoryStateException, InvalidConfigurationException,
			DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException,
			NoHeadException, TransportException, GitAPIException {
		PullCommand pullCommand = git.pull();
		pullCommand.call();
	}

	/**
	 * 
	 * Pushes the committed changes to the remote repository.
	 * 
	 * @param username
	 *            for the remote repository
	 * @param password
	 *            for the remote repository
	 * @throws InvalidRemoteException
	 * @throws TransportException
	 * @throws GitAPIException
	 */
	public void push(String username, String password) throws InvalidRemoteException,
			TransportException, GitAPIException {
		PushCommand pushCommand = git.push();
		UsernamePasswordCredentialsProvider credentials = new UsernamePasswordCredentialsProvider(
				username, password);
		pushCommand.setCredentialsProvider(credentials);
		pushCommand.call();
	}

	/**
	 * 
	 * Tries to rebase the selected branch on top of the current one.
	 * 
	 * @param name
	 *            the branch to rebase
	 * @throws NoHeadException
	 * @throws WrongRepositoryStateException
	 * @throws GitAPIException
	 */
	public void rebase(String name) throws NoHeadException, WrongRepositoryStateException,
			GitAPIException {
		RebaseCommand rebaseCommand = git.rebase();
		rebaseCommand.setOperation(Operation.BEGIN);
		rebaseCommand.setUpstream(name);
		rebaseCommand.call();
	}

	/**
	 * 
	 * Get the current status of the Git repository.
	 * 
	 * @return {@link org.eclipse.jgit.api.Status} object
	 * @throws NoWorkTreeException
	 * @throws GitAPIException
	 */
	public Status status() throws NoWorkTreeException, GitAPIException {
		return git.status().call();
	}

	/**
	 * 
	 * Returns the SHA of the last commit on the specified branch.
	 * 
	 * @param branch
	 *            the name of the specified branch
	 * @return SHA example: "21d5a96070353d01c0f30bc0559ab4de4f5e3ca0"
	 * @throws RefAlreadyExistsException
	 * @throws RefNotFoundException
	 * @throws InvalidRefNameException
	 * @throws CheckoutConflictException
	 * @throws GitAPIException
	 */
	public String getLastSHAForBranch(String branch) throws RefAlreadyExistsException,
			RefNotFoundException, InvalidRefNameException, CheckoutConflictException,
			GitAPIException {
		return checkout(branch).getLeaf().getObjectId().getName();
	}
}