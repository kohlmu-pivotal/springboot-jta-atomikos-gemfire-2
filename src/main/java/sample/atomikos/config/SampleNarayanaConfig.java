package sample.atomikos.config;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.GemFireCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;

@Configuration
public class SampleNarayanaConfig {
	@Bean
	public PartitionedRegionFactoryBean getTestRegion(GemFireCache cache) {
		PartitionedRegionFactoryBean<Object, Object> regionFactoryBean = new PartitionedRegionFactoryBean<>();
		regionFactoryBean.setCache(cache);
		regionFactoryBean.setName("testRegion");
		regionFactoryBean.setDataPolicy(DataPolicy.PARTITION);
		return regionFactoryBean;
	}
}
