package nz.co.senanque.jclouds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;

@RunWith(SpringRunner.class)
@TestPropertySource
@ContextConfiguration(classes={JcloudsHelperConfig.class})
public class JcloudsHelperTest {
	
	@Autowired JcloudsHelper jcloudsHelper;

	@Test
	public void test() throws IOException {
		final BlobStoreContext context = jcloudsHelper.getBlobStoreContext();
		final BlobStore blobStore = context.getBlobStore();
		Assert.assertTrue("failed to create container",blobStore.createContainerInLocation(null, "mycontainer"));
		
		final ByteSource payload = ByteSource.wrap("blob-content".getBytes(Charsets.UTF_8));
		final Blob blob = blobStore.blobBuilder("test") // you can use folders via blobBuilder(folderName + "/sushi.jpg")
		    .payload(payload)
		    .contentLength(payload.size())
		    .build();

		// Upload the Blob
		blobStore.putBlob("mycontainer", blob);
		
		final Blob fetched = blobStore.getBlob("mycontainer", "test");
		InputStream inputStream = fetched.getPayload().openStream();
		String text = new BufferedReader(
			      new InputStreamReader(inputStream, StandardCharsets.UTF_8))
			        .lines()
			        .collect(Collectors.joining("\n"));
		Assert.assertEquals("string is different", "blob-content", text);
		context.close();
	}

}
