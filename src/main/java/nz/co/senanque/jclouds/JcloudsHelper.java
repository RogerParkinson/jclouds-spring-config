/**
 * 
 */
package nz.co.senanque.jclouds;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.domain.Credentials;
import org.jclouds.googlecloud.GoogleCredentialsFromJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Supplier;
import com.google.common.io.Files;

import lombok.SneakyThrows;

/**
 * @author Roger Parkinson
 */
@Component
public class JcloudsHelper {

	@Value("${jclouds.provider:google-cloud-storage}")
	String provider;
	@Value("${GOOGLE_APPLICATION_CREDENTIALS:}")
	String jsonKeyFile;
	@Value("${jclouds.identity:}")
	String identity;
	@Value("${jclouds.credential:}")
	String credential;
	Supplier<Credentials> credentialsSupplier;
	
	ThreadLocal<BlobStoreContext> blobStoreContext = new ThreadLocal<>();
	
	@PostConstruct
	@SneakyThrows
	public void init() {
		switch (provider) {
		case "google-cloud-storage":
			Assert.hasText(jsonKeyFile, "JcloudsHelper Missing GOOGLE_APPLICATION_CREDENTIALS for provider: "+provider);
			credentialsSupplier = new GoogleCredentialsFromJson(Files.asCharSource(new File(jsonKeyFile), Charset.defaultCharset()).read());
			break;
		case "aws-s3":
		case "transient":
			Assert.hasText(identity, "JcloudsHelper Missing identity for provider: "+provider);
			Assert.hasText(credential, "JcloudsHelper Missing credential for provider: "+provider);
			credentialsSupplier = new Supplier<Credentials>() {
			    @Override
			    public Credentials get() {
			        return new Credentials.Builder<Credentials>().identity(identity).credential(credential).build();
			    }
			};
			break;
		}
	}
	
	public BlobStoreContext getBlobStoreContext() {
		if ("transient".equals(provider)) {
			return Optional.ofNullable(blobStoreContext.get()).orElseGet(() -> getBlobStoreContextThreaded());
		}
		return getBlobStoreContextInternal();
	}
	
	private BlobStoreContext getBlobStoreContextThreaded() {
		blobStoreContext.set(getBlobStoreContextInternal());
		return blobStoreContext.get();
	}
	
	private BlobStoreContext getBlobStoreContextInternal() {
		return ContextBuilder.newBuilder(provider).credentialsSupplier(credentialsSupplier)
			    .buildView(BlobStoreContext.class);
		
	}

	public JcloudsHelper init(String provider, String identity, String credential, String container) {
		this.provider = provider;
		this.identity = identity;
		this.credential = credential;
		init();
		if (container != null) {
			final BlobStoreContext context = getBlobStoreContext();
			final BlobStore blobStore = context.getBlobStore();
			blobStore.createContainerInLocation(null, container);
		}
		return this;
	}
	public JcloudsHelper init(String jsonKeyFile) {
		this.provider = "google-cloud-storage";
		this.jsonKeyFile = jsonKeyFile;
		init();
		return this;
	}
	
}
