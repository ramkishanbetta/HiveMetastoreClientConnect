package com.betta.bigdata.hive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;


public class HiveMetastoreClient {
	
	private static final Logger LOGGER = LogManager.getLogger(HiveMetastoreClient.class) ;

	public static void main(String args[]) {

		Options options = new Options();
		 // Help or usage 
		 options.addOption("help", false, "Help or Usage method");
		 
		 //File and Directory Operations
		 options.addOption("hiveDB", true, "Hive DB or HaaS instance name");
		 
//		 Option properties = OptionBuilder.withArgName("properties> <values").withValueSeparator(';').hasOptionalArgs(1000)
//				 .withDescription("Please enter the properties required - tableName").create("properties");
//		 options.addOption(properties);		 
		 
		 		 
	     // Time to parse the commands issued by user
		 CommandLineParser parser = new BasicParser();
		 CommandLine cmd;
			try {
				cmd = parser.parse(options, args);
			}catch (ParseException pe){ 
				usage(options);
				return; 
				
		 }
			
		String hiveDB = null;
		if(!cmd.hasOption("hiveDB")){
			
			usage(options);
			return;
		}
		hiveDB = cmd.getOptionValue("hiveDB");		
		
//		String [] propertyValues = cmd.getOptionValues("properties");
//		boolean allRequired = propertyValues[0].equalsIgnoreCase("all")?true:false;
		
		String path = null;
		String source = null;
		String destination = null;
		int mode = -1;
		String owner = null;
		String group = null;
		String command = null;
		System.setProperty("java.security.auth.login.config", "gss-jaas.conf");
		System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
		System.setProperty("java.security.krb5.conf", "krb5.conf");

		
		ArrayList<String> dbNames = new ArrayList<>();
		ArrayList<String> dbTableNames = new ArrayList<>();
		ArrayList<String> tempTableNames = new ArrayList<>();
		ArrayList<String> dbTableColNames = new ArrayList<>();
		ArrayList<org.apache.hadoop.hive.metastore.api.FieldSchema> tempdbTableColNames = new ArrayList<>();
		Pattern p = null;
		System.out.println("Started..");
		
		
		File hiveConfFile = new File("hive-site.xml");
		System.out.println(hiveConfFile.getAbsolutePath());
		

		try {
			InputStream is = new FileInputStream(hiveConfFile);
//			Configuration config = new Configuration();
//			config.se
			
			HiveConf hive_conf = new HiveConf();
			
			hive_conf.addResource(new File("hive-site.xml").toURI().toURL());
			System.out.println(hive_conf.get("hive.metastore.uris"));
//			hive_conf.addResource(is);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss SSS");
			HiveMetaStoreClient hmc = new HiveMetaStoreClient(hive_conf);
			
			System.out.println("DB Name -"+hiveDB.toLowerCase());
			dbNames = (ArrayList) hmc.getDatabases(hiveDB.toUpperCase());
			StringBuilder sb = new StringBuilder();
			sb.append("TableName");
			sb.append("#");
			sb.append("Table Type");
			sb.append("#");
			sb.append("Owner");
			sb.append("#");
			sb.append("Create Time");
			sb.append("#");
			sb.append("Last Access ");
			sb.append("#");
			sb.append("Partition Key Size");
			sb.append("#");
			sb.append("Partition Keys");
			sb.append("#");
			sb.append("Column Size");
			sb.append("#");
			sb.append("Location");
			sb.append("#");
			sb.append("Input Format");
			sb.append("#");
			sb.append("Output Format");
			sb.append("#");
			sb.append("\n");
			
			for (String dbName : dbNames) {
				
				System.out.println("Got all the DBs - "+hiveDB.toUpperCase());
				
				tempTableNames = (ArrayList) hmc.getAllTables(dbName);

				for (String tableName : tempTableNames){
					
					System.out.println("***Table"+tableName);
					Table table = hmc.getTable(dbName, tableName);
					sb.append(tableName);
//					if(allRequired || propertyValues)
					sb.append("#");
					sb.append(table.getTableType());
					sb.append("#");
					sb.append(table.getOwner());
					sb.append("#");
					sb.append(sdf.format(new Date(table.getCreateTime())));
					sb.append("#");
					sb.append(sdf.format(new Date(table.getLastAccessTime())));
					sb.append("#");
					sb.append(table.getPartitionKeysSize());
					sb.append("#");
					sb.append(table.getPartitionKeys());
					sb.append("#");
					sb.append(table.getSd().getColsSize());
					sb.append("#");
					sb.append(table.getSd().getLocation());
					sb.append("#");
					sb.append(table.getSd().getInputFormat());
					sb.append("#");
					sb.append(table.getSd().getOutputFormat());
					sb.append("\n");
					
					
					
					/*List<FieldSchema> fields = (List<FieldSchema>) hmc.getFields(hiveDB, tableName);
					for(FieldSchema field : fields){
						
						field.getComment();
						field.getName();
						field.getType();
					}*/
					
				}

					

			}

			// for (String dbTableColName : dbTableColNames) {
			/*
			 * p = Pattern.compile("bill");
			 * 
			 * Matcher m = p.matcher(dbTableColName);
			 * 
			 * new File("sensitiveColumnList.txt").delete();
			 * 
			 * while (m.find()) { matchingdbTableColNames.add(dbTableColName); }
			 */
			
			File file = new File("TableList.txt");
			if(file.exists()){
				
				file.delete();
			}
			file.createNewFile();

			try {
				FileWriter fw = new FileWriter(file);
				fw.write(sb.toString());
				fw.flush();
				fw.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// }
			System.out.println("Completed, please check file");
		} catch (MetaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private static void usage(Options options) {
		// Use the inbuilt formatter class
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("HiveMetastoreClient", options);
	}
}