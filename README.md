# jclouds-spring-config
Configure Apache Jclouds blob storage using Spring

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/nz.co.senanque/jclouds-spring-config/badge.svg)](http://mvnrepository.com/artifact/nz.co.senanque/jclouds-spring-config)

[![Java with Nexus Repository](https://github.com/RogerParkinson/jclouds-spring-config/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/RogerParkinson/jclouds-spring-config/actions/workflows/maven-publish.yml)

## Purpose

Provides a simple helper that can be injected with parameters to determine what kind of bucket to access and any necessary credentials. The helper
returns a `BlobStoreContext` that can be used to access the buckets.

## Usage

```
	@Autowired JcloudsHelper jcloudsHelper;
...

	final BlobStoreContext context = jcloudsHelper.getBlobStoreContext();
...
```

## Configuration

There are up to three variables to configure, but it varies depending on the bucket provider.

### transient

Use a properties file like this: 

```
jclouds.provider=transient
jclouds.identity=whatever
jclouds.credential=whatever
```

This uses an in-memory bucket, probably only useful for test code. The `identity` and the `credential` are not actually used but they must be present.
As is usual with Spring you can define these as environment variables or add them to a yaml file if that works better for you.

### google-cloud-storage

GCP Buckets are configured by having an account key saved in a json file and pointed to by the environment variable `GOOGLE_APPLICATION_CREDENTIALS` eg

```
export GOOGLE_APPLICATION_CREDENTIALS=./account.json
```

This is all you have to do because `jclouds.provider` defaults to `google-cloud-storage`, ie no properties file needed.

### aws-s3

Use a properties file like this: 

```
jclouds.provider=aws-s3
jclouds.identity=whatever
jclouds.credential=whatever
```

This uses an AWS s3 bucket. You need to give user identity and credentials values obtained from the AWS Console, and the user must have access to the target buckets.
As is usual with Spring you can define these as environment variables or add them to a yaml file if that works better for you.

## Other bucket providers

These will be added as need and time allows.



