/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.atomikos;

import javax.annotation.PostConstruct;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import com.atomikos.icatch.jta.UserTransactionManager;

import org.apache.geode.cache.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableGemFireAsLastResource;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import sample.atomikos.config.SampleNarayanaConfig;
import sample.atomikos.jndi.SimpleNamingContextBuilder;

@SpringBootApplication
@EnableTransactionManagement(order = 1)
@CacheServerApplication
@EnableGemFireAsLastResource
@EnableManager(start = true)
public class SampleNarayanaApplication {

	private static final Logger LOG = LoggerFactory.getLogger(SampleNarayanaApplication.class);

	// In-Memory JNDI service used by Gemfire to lookup global transactions.
	private static SimpleNamingContextBuilder inMemoryJndiBuilder;

	// Note: the SimpleNamingContextBuilder MUST be created before the Spring Application Context!!!
	static {
		try {
			inMemoryJndiBuilder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		}
		catch (NamingException e) {
			LOG.error("Failed to create in-memory JNDI provider", e);
		}
	}

	private final TransactionManager atomikosTxManager;

	public SampleNarayanaApplication(TransactionManager atomikosTxManager) {
		this.atomikosTxManager = atomikosTxManager;
	}

	@PostConstruct
	public void registerNarayanaUserTransaction() {
		// Gemfire uses JNDI:java:comp/UserTransaction to lookup global transactions.
		inMemoryJndiBuilder.bind("java:comp/UserTransaction", atomikosTxManager);
	}

	public static void main(String[] args) throws Exception {
		new SpringApplicationBuilder(SampleNarayanaApplication.class)
			.web(WebApplicationType.NONE)
			.build()
			.run(args).close();
	}


	@Bean
	public ApplicationRunner run(AccountService service, AccountRepository repository, Region region) {
		return args -> {
			service.createAccountAndNotify("josh", region);
			LOG.info("Count is " + repository.count());
			try {
				// Using username "error" will cause service to throw SampleRuntimeException
				service.createAccountAndNotify("error", region);
			}
			catch (SampleRuntimeException ex) {
				// Log message to let test case know that exception was thrown
				LOG.error(ex.getMessage());
			}
			LOG.info("Count is " + repository.count());
		};
	}
}
