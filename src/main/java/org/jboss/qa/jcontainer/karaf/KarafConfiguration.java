/*
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.qa.jcontainer.karaf;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import org.jboss.qa.jcontainer.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class KarafConfiguration extends Configuration {

	public static final Integer DEFAULT_MANAGEMENT_PORT = 8101;
	public static final Integer DEFAULT_PORT = 8181;

	protected final int managementPort;
	protected final File keyFile;
	protected final File script;

	protected KarafConfiguration(Builder<?> builder) {
		super(builder);
		script = builder.script;
		managementPort = builder.managementPort;
		//Optional
		keyFile = builder.keyFile;
	}

	public int getManagementPort() {
		return managementPort;
	}

	public static Builder<?> builder() {
		return new Builder2();
	}

	public File getKeyFile() {
		return keyFile;
	}

	@Override
	public List<String> generateCommand() {
		if (!script.exists()) {
			throw new IllegalStateException(String.format("Script '%s' does not exist", script.getAbsolutePath()));
		}
		final List<String> cmd = new ArrayList<>();
		if (SystemUtils.IS_OS_WINDOWS) {
			cmd.add("cmd");
			cmd.add("/c");
			cmd.add(script.getAbsolutePath());
		} else {
			cmd.add("bash");
			cmd.add(script.getAbsolutePath());
		}
		return cmd;
	}

	public abstract static class Builder<T extends Builder<T>> extends Configuration.Builder<T> {
		protected int managementPort;
		protected File keyFile;
		protected File script;

		public Builder() {
			xms = "128m";
			xmx = "512m";
			port = DEFAULT_PORT;
			managementPort = DEFAULT_MANAGEMENT_PORT;
			username = "karaf";
			password = "karaf";
		}

		public T managementPort(int managementPort) {
			this.managementPort = managementPort;
			return self();
		}

		public T keyFile(String keyFile) {
			this.keyFile = new File(keyFile);
			return self();
		}

		public KarafConfiguration build() {
			script = new File(directory, "bin" + File.separator + (SystemUtils.IS_OS_WINDOWS ? "karaf.bat" : "karaf"));
			if (!StringUtils.isEmpty(xms)) {
				envProps.put("JAVA_MIN_MEM", xms);
			}
			if (!StringUtils.isEmpty(xmx)) {
				envProps.put("JAVA_MAX_MEM", xmx);
			}
			if (!StringUtils.isEmpty(permSize)) {
				envProps.put("JAVA_PERM_MEM", permSize);
			}
			if (!StringUtils.isEmpty(maxPermSize)) {
				envProps.put("JAVA_MAX_PERM_MEM", maxPermSize);
			}
			return new KarafConfiguration(this);
		}
	}

	private static class Builder2 extends Builder<Builder2> {
		@Override
		protected Builder2 self() {
			return this;
		}
	}
}
