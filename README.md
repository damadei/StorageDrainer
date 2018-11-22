![Logo](https://azure.microsoft.com/svghandler/storage-blobs?width=200&height=115)
# StorageDrainer
A tool to migrate object storage data from other platforms (AWS, GCP, etc) to Azure Blob Storage.  Currently supports migrating AWS S3 to Azure Blob Storage in a distributed & scalable fashion, controlled by Apache Spark.

## What's new in latest version (v1.2)
* Support for copying from one Azure Storage Account to another while GZipping the files

## Key Features and Benefits
In summary, these are the main features you will find in StorageDrainer and are the reasons you should use it in your solutions:
* Parallel inventory & copy leveraging Spark’s capabilities
* Full or selective migration:
* Able to specify which folders to copy via a parameters file
* Resubmit the migration from where it stopped w/o starting from scratch
* Incremental migration with automatic inventory comparing which files are missing and copying only the delta
* Inventory reporting producing a file with the differences or reporting the size in # of files or storage consumed

How to use it? Let's see:

# **Using pre-built or building from the sources?**
You have two options to use the Azure Storage Drainer: the pre-compiled version or building from the sources.

# **Using the pre-compiled version**
Download the pre-compiled latest version [here](https://github.com/damadei/StorageDrainer/blob/master/dist/StorageDrainer-1.2.0-jar-with-dependencies.jar).

# **Building from The Sources**
To build the project from the sources:

Make sure you have Java 8+, Maven and Git installed and on the path.

1. Clone the project from the sources
    `git clone https://github.com/damadei/StorageDrainer`

1. Compile the project into a single assembly file:
    `mvn clean compile assembly:single`

1. The resulting jar will be in `target` folder and will be named `StorageDrainer-<version>-jar-with-dependencies.jar`

# **Provisioning HDInsight**
This project was built to be used with Azure HDInsight. When provisioning HDInsight, make sure you:

1. Provision the cluster type as HD Insight v3.6 + Apache Spark v2.3+
1. Take note of the cluster login username, password and ssh username
1. Provision the cluster with an associated storage account. This is where we will place the JAR and text parameters for the service.


# **Configuring HDInsight with the Project**
There are some configuration steps required for HDInsight to be used, they are detailed bellow.
1. Make sure you provisioned HDInsight with an associated storage account.
1. Copy the JAR generated in step above to the associated storage storage account. You can also use the provided latest version JAR, located in the /dist folder. We recommend uploading the file to a new directory called `user-custom/jars` under the `default container` of the storage account associated to with the HDInsight cluster. This directory will be referred to when submitting the job by pointing to the JAR file of the project.
1. If there are input parameter files to be used by the copy (to list the files which should be copied), we recommend placing them in the `default container` in a folder called `user-custom/input`.

# **Extra Configuration Required on HDInsight**
HDInsight will require extra configuration steps to be able to execute the project. For this: 
1. Go to the Ambari views of the cluster then go to **Services**, **Spark2**.
1. Find the `Custom spark2-defaults` and **add** two new properties: 
    `spark.driver.userClassPathFirst = true`
    `spark.executor.userClassPathFirst = true`

1. Still in the Custom spark2-defaults find the `spark.executor.cores` and change it to the number of threads you want to execute per Spark container. In our tests the best results were, the number of  cores you have per node - 1.

1. Find also the `spark.executor.instances` property and change it to the number of containers you want executing in **each worker node**. In our tests the best result were **3 * # of worker nodes** but you can increase based on your scenario and measure the load in each node.

1. Find the `livy.server.csrf_protection.enabled` property and change it to false. If this is an HDInsight you are using for other things, I recommend you change this property back to true after executing the copy as changing it to false may lead to cross-site request forgery security issues so it's best to leave it as true after submiting the jobs. 

1. Remember to restart the cluster when finished by clicking the orange button at the top.


# **Job Submission**
After configuring the HDInsight environment as instructed above, it's time to submit the job.

To submit the job, we'll use the Livy tool which is a REST interface designed to submit Spark jobs.

The easiest way to create the call to Livy is via Postman. 

1. Create a new request in postman
1. Change the request type to POST
1. Use as URL the value `https://<your hdi clustername>.azurehdinsight.net/livy/batches`
1. Configure Basic authentication and as username enter your HDInsight username and its password in the correponding field
1. In Headers, enter a `Content-Type` header and as value enter `application/json`
1. Still in Headers, enter a `X-Requested-By` header and as value enter the HDInsight user name
1. In Body enter the following structure (please note that parameters are dependent on the job driver you are using, see the respective pages for samples and also for the list of possible parameters:
<pre>
   {
	"className": "<name of the job class to execute>",
	"file": "wasb:///user-custom/jars/<Name of the Jar with Version>",
	    "args": [
		"argument1 name", "argument1 value", 
		"argument2 name", "argument2 value",
		"...", "...",
	 ]
   }
</pre>

That's it. Bellow we describe each job driver type supported by the StorageDrainer tool.

# **Migration**
The Migration job performs an assessment on the different files and perform the copy of the files considered different (different size) or missing in the target container in Azure Blob Storage compared to the source bucket in AWS S3.

## Job arguments and configuration
The following items are required to submit a job to perform a migration
- **className:** `com.microsoft.ocp.storage.drainer.MigrationJobDriver`
- **file:** Is the **name of the JAR file of the Storage Drainer tool**. depends on the JAR name & version and folder where the JAR was uploaded to storage. For example, with an **1.2.0 JAR** version and JAR uploaded to the **user-custom/jars** folder this would be `wasb:///user-custom/jars/StorageDrainer-1.2.0-jar-with-dependencies.jar`. The wasb:/// prefix indicates this is in the default Azure Blob Storage account and container.

### Arguments
### Mandatory
- -s3bucket: name of the source s3 bucket.
- -awsaccid: source AWS account id.
- -awsacckey: source AWS account key.
- -awsregion: AWS region where the bucket is located.
- -targetazkey: Destination Azure Storage account key.
- -targetblobcontainer: target container in Azure. If it does not exist, will be created
- -lp: number of partitions to perform the files listing. Recommended value to start with a default HDInsight cluster is 50.
- -cp: number of partitions to perform the copy. Recommended value to start with a default HDInsight cluster is 200.

### Optional
- -f: input file containing the virtual directories to copy. The file should be placed in the default storage account associated with the HDInsight Cluster in the default container, preferably in folder /user-custom/input. In this case you refer to it via this parameter as `wasb:///user-custom/input/myInputFile.txt`.

### Sample
<pre>
{
	"className": "com.microsoft.ocp.storage.drainer.MigrationJobDriver",
	"file": "wasb:///user-custom/jars/StorageDrainer-1.2.0-jar-with-dependencies.jar",
	"args": [
		"-s3bucket", "sourceBucketName",
		"-awsaccid", "AAAAAAAAAAAAAAAAA",
		"-awsacckey", "AAAA+123+AAAAAAAAAXXXXXXX",
        "-awsregion", "us-east-1",
		"-targetazkey", "DefaultEndpointsProtocol=https;AccountName=xxxx;AccountKey=12345XZXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX==;EndpointSuffix=core.windows.net",
		"-targetblobcontainer", "targetcontainername",
		"-lp", "50",
		"-cp", "200"
	]
}
</pre>



# **Azure to Azure Copy**
The Azure to Azure copy copies the files from different Azure storage accounts or to different containers in the same account.

## Job arguments and configuration
The following items are required to submit a job to perform a copy from Azure to Azure:
- **className:** `com.microsoft.ocp.storage.drainer.AzureToAzureCopyDriver`
- **file:** Is the **name of the JAR file of the Storage Drainer tool**. depends on the JAR name & version and folder where the JAR was uploaded to storage. For example, with an **1.2.0 JAR** version and JAR uploaded to the **user-custom/jars** folder this would be `wasb:///user-custom/jars/StorageDrainer-1.2.0-jar-with-dependencies.jar`. The wasb:/// prefix indicates this is in the default Azure Blob Storage container.

### Arguments
### Mandatory
- -sourceazkey: Source Azure Storage account key.
- -sourceblobcontainer: Source container name in Azure. 
- -targetazkey: Target Azure Storage account key. If copying to a different container in the same account, just repeat the same key used as sourceazkey.
- -targetblobcontainer: Target container in Azure. If it does not exist, will be created.
- -lp: number of partitions to perform the files listing. Recommended value to start with a default HDInsight cluster is 50.
- -cp: number of copy partitons to perform the copy. Recommended value to start with a default HDInsight cluster is 200.

### Optional
- -f: input file containing the virtual directories to copy. The file should be placed in the default storage account associated with the HDInsight Cluster in the default container, preferably in folder /user-custom/input. In this case you refer to it via this parameter as `wasb:///user-custom/input/myInputFile.txt`.

### Sample
<pre>
{
	"className": "com.microsoft.ocp.storage.drainer.AzureToAzureCopyDriver",
	"file": "wasb:///user-custom/jars/StorageDrainer-1.2.0-jar-with-dependencies.jar",
	"args": [
		 "-sourceazkey", "DefaultEndpointsProtocol=https;AccountName=xxxxsource;AccountKey=12345XZXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX==;EndpointSuffix=core.windows.net",
		 "-sourceblobcontainer" "sourcecontainername",
		 "-targetazkey", "DefaultEndpointsProtocol=https;AccountName=xxxxtarget;AccountKey=12345XZXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX==;EndpointSuffix=core.windows.net",
		 "-targetblobcontainer", "targetcontainername",
		 "-lp", "50",
		 "-cp", "200"
    ]
}
</pre>


# **Azure to Azure Copy with Compression (GZipping)**
The Azure to Azure copy with GZip support copies the files from different Azure storage accounts or to different containers in the same account while gzipping the results and marking the file encoding as gzip.

## Job arguments and configuration
The following items are required to submit a job to perform a copy from Azure to Azure:
- **className:** `com.microsoft.ocp.storage.drainer.AzureToAzureGZipDriver`
- **file:** Is the **name of the JAR file of the Storage Drainer tool**. depends on the JAR name & version and folder where the JAR was uploaded to storage. For example, with an **1.2.0 JAR** version and JAR uploaded to the **user-custom/jars** folder this would be `wasb:///user-custom/jars/StorageDrainer-1.2.0-jar-with-dependencies.jar`. The wasb:/// prefix indicates this is in the default Azure Blob Storage container.

### Arguments
### Mandatory
- -sourceazkey: Source Azure Storage account key.
- -sourceblobcontainer: Source container name in Azure. 
- -targetazkey: Target Azure Storage account key. If copying to a different container in the same account, just repeat the same key used as sourceazkey.
- -targetblobcontainer: Target container in Azure. If it does not exist, will be created.
- -lp: number of partitions to perform the files listing. Recommended value to start with a default HDInsight cluster is 50.
- -cp: number of copy partitons to perform the copy. Recommended value to start with a default HDInsight cluster is 200.

### Optional
- -f: input file containing the virtual directories to copy. The file should be placed in the default storage account associated with the HDInsight Cluster in the default container, preferably in folder /user-custom/input. In this case you refer to it via this parameter as `wasb:///user-custom/input/myInputFile.txt`.

### Sample
<pre>
{
	"className": "com.microsoft.ocp.storage.drainer.AzureToAzureGZipDriver",
	"file": "wasb:///user-custom/jars/StorageDrainer-1.2.0-jar-with-dependencies.jar",
	"args": [
		 "-sourceazkey", "DefaultEndpointsProtocol=https;AccountName=xxxxsource;AccountKey=12345XZXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX==;EndpointSuffix=core.windows.net",
		 "-sourceblobcontainer" "sourcecontainername",
		 "-targetazkey", "DefaultEndpointsProtocol=https;AccountName=xxxxtarget;AccountKey=12345XZXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX==;EndpointSuffix=core.windows.net",
		 "-targetblobcontainer", "targetcontainername",
		 "-lp", "50",
		 "-cp", "200"
    ]
}
</pre>


# **Inventory**
The Inventory job performs an assessment on the different files between the source and target and generates output files with the differences found. Can be used to compare from AWS to Azure, Azure to AWS and also Azure to Azure.

## Job arguments and configuration
The following items are required to submit a job to perform the inventory:
- **className**: com.microsoft.ocp.storage.drainer.InventoryJobDriver
- **file:** Is the **name of the JAR file of the Storage Drainer tool**. depends on the JAR name & version and folder where the JAR was uploaded to storage. For example, with an **1.2.0 JAR** version and JAR uploaded to the **user-custom/jars** folder this would be `wasb:///user-custom/jars/StorageDrainer-1.2.0-jar-with-dependencies.jar`. The wasb:/// prefix indicates this is in the default Azure Blob Storage container.

### Arguments - AWS to Azure and Azure to AWS
### Mandatory
- -s3bucket: name of the source s3 bucket
- -awsaccid: source AWS account id
- -awsacckey: source AWS account key
- -awsregion: AWS region where the bucket is located
- -targetazkey: Destination Azure Storage account key
- -targetblobcontainer: target container in Azure. If does not exist, will be created
- -lp: number of partitions to perform the files listing. Recommended value to start with a default HDInsight cluster is 50.
- -o: output path where to store the results. Recommended to be `wasb:///user-custom/output`
- -direction: can be **aws-to-azure**, **azure-to-aws** or **azure-to-azure**. This determines where the first list is obtained to compare with the other. Generally will should be aws-to-azure to check which files changed or were added in AWS that are not in Azure.

### Arguments - Azure to Azure
### Mandatory
- -sourceazkey: Source Azure Storage account key.
- -sourceblobcontainer: Source Azure Storage container name.
- -targetazkey: Destination Azure Storage account key.
- -targetblobcontainer: target container in Azure. If it does not exist, will be created
- -lp: number of partitions to perform the files listing. Recommended value to start with a default HDInsight cluster is 50.
- -o: output path where to store the results. Recommended to be `wasb:///user-custom/output`
- -direction: can be **aws-to-azure**, **azure-to-aws**, **azure-to-azure**. This determines where the first list is obtained to compare with the other. Generally will should be aws-to-azure to check which files changed or were added in AWS that are not in Azure.

### Optional
- -f: input file containing the virtual directories to copy. The file should be placed in the default storage account associated with the HDinsight Cluster in the default container, preferably in folder /user-custom/input. In this case you refer to it via this parameter as `wasb:///user-custom/input/myInputFile.txt`.

### Sample (AWS to Azure)
<pre>
{
	"className": "com.microsoft.ocp.storage.drainer.InventoryJobDriver",
	"file": "wasb:///user-custom/jars/StorageDrainer-1.2.0-jar-with-dependencies.jar",
	"args": [
		"-s3bucket", "yourSourceBucket",
		"-awsaccid", "AAAAAAAAAAAAAAAAA",
		"-awsacckey", "AAAA+123+AAAAAAAAAXXXXXXX",
		"-awsregion", "us-east-1",
		"-targetazkey", "DefaultEndpointsProtocol=https;AccountName=xxxx;AccountKey=AAAAAAAAAAAAAAAAAAAAA11111111111111111==;EndpointSuffix=core.windows.net",
		"-targetblobcontainer", "targetcontainer",
		"-lp", "50",
		"-direction", "aws-to-azure",
		"-o", "wasb:///user-custom/output"
	]
}
</pre>


# **AWS Sizing**
This job performs an assessment and calculates the total number of files and total storage that resides in AWS

## Job arguments and configuration
The following items are required to submit an AWS Sizing job:
- **className**: com.microsoft.ocp.storage.drainer.AwsSizingJobDriver
- **file:** Is the **name of the JAR file of the Storage Drainer tool**. depends on the JAR name & version and folder where the JAR was uploaded to storage. For example, with an **1.2.0 JAR** version and JAR uploaded to the **user-custom/jars** folder this would be `wasb:///user-custom/jars/StorageDrainer-1.2.0-jar-with-dependencies.jar`. The wasb:/// prefix indicates this is in the default Azure Blob Storage container.

### Arguments
### Mandatory
- -s3bucket: name of the s3 bucket
- -awsaccid: AWS account id
- -awsacckey: AWS account key
- -awsregion: AWS region where the bucket is located
- -lp: number of partitions to perform the files listing. Recommended value to start with a default HDInsight cluster is 50.

### Optional
- -f: input file containing the virtual directories to copy. The file should be placed in the default storage account associated with the HDInsight Cluster in the default container, preferably in folder /user-custom/input. In this case you refer to it via this parameter as `wasb:///user-custom/input/myInputFile.txt`.

### Sample
<pre>
{
	"className": "com.microsoft.ocp.storage.drainer.AwsSizingJobDriver",
	"file": "wasb:///user-custom/jars/StorageDrainer-1.2.0-jar-with-dependencies.jar",
	"args": [
		"-s3bucket", "bucketname",
		"-awsaccid", "AAAAAAAAAAAAAAAAA",
		"-awsacckey", "AAAA+123+AAAAAAAAAXXXXXXX",
		"-awsregion", "us-east-1",
		"-lp", "50"
	]
}
</pre>

### Getting the Results
The results of the sizing jobs are reported via the Job driver logging. To access it you should:

- Go to your HDInsight portal page
- Click on Cluster Dashboard
- Click on YARN
- Find your job in the list via its class name and click it
- Click **logs**
- Find the third occurrence where it says **Click here for the full log.** and click the **here** link
- Search for **The aggregate value is**
- You should see the total number of files and storage. For example: `SizingJobDriverBase: ############# The aggregate value is: 500 files. Size: 2.4 GB (2621440000 bytes) #############`


# **Azure Sizing**
This job performs an assessment and calculates the total number of files and total storage that resides in the Azure Blob Storage container you specify and can take into consideration just the folders you specify in an (optional) input file

## Job arguments and configuration
The following items are required to submit a job to perform an Azure Sizing
- **className**: com.microsoft.ocp.storage.drainer.AzureSizingJobDriver
- **file:** Is the **name of the JAR file of the Storage Drainer tool**. depends on the JAR name & version and folder where the JAR was uploaded to storage. For example, with an **1.2.0 JAR** version and JAR uploaded to the **user-custom/jars** folder this would be `wasb:///user-custom/jars/StorageDrainer-1.2.0-jar-with-dependencies.jar`. The wasb:/// prefix indicates this is in the default Azure Blob Storage container.

### Arguments
### Mandatory
- -targetazkey: Destination Azure Storage account key.
- -targetblobcontainer: target container in Azure. If it does not exist, will be created.
- -lp: number of partitions to perform the files listing. Recommended value to start with a default HDInsight cluster is 50.

### Optional
- -f: input file containing the virtual directories to copy. The file should be placed in the default storage account associated with the HDinsight Cluster in the default container, preferably in folder /user-custom/input. In this case you refer to it via this parameter as `wasb:///user-custom/input/myInputFile.txt`.

### Sample
<pre>
{
	"className": "com.microsoft.ocp.storage.drainer.AzureSizingJobDriver",
	"file": "wasb:///user-custom/jars/StorageDrainer-1.2.0-jar-with-dependencies.jar",
	"args": [
		"-targetazkey", "DefaultEndpointsProtocol=https;AccountName=xxxxx;AccountKey=XXXXAAAA111111AAAAAAAAAAAAAA==;EndpointSuffix=core.windows.net",
		"-targetblobcontainer", "mycontainer",
		"-lp", "50"
	]
}
</pre>

### Getting the Results
The results of the sizing jobs are reported via the Job driver logging. To access it you should:

- Go to your HDInsight portal page
- Click on Cluster Dashboard
- Click on YARN
- Find your job in the list via its class name and click it
- Click **logs**
- Find the third occurrence where it says **Click here for the full log.** and click the **here** link
- Search for **The aggregate value is**
- You should see the total number of files and storage. For example: `SizingJobDriverBase: ############# The aggregate value is: 500 files. Size: 2.4 GB (2621440000 bytes) #############`


# **Monitoring**
The jobs can be monitored via Yarn monitoring
- Go to your HDInsight portal page
- Click on Cluster Dashboard
- Click on YARN
- Find your job in the list via its class name. Here you have the information if the process is - Click the Attempt ID
 or **FINISHED** and also if it was **SUCCESSFUL** or **FAILED** and click it
- Click the Attempt ID
- If the process is still running, you can view the running containers and access their logs
- If the process is finished you can access only the logs for the attempt to inspect errors