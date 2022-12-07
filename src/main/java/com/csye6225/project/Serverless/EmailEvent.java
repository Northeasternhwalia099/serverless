package com.csye6225.project.Serverless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
//testcrun

public class EmailEvent {

    AmazonDynamoDB dynamodbClient;

    private static final Logger LOG = LoggerFactory.getLogger(EmailEvent.class);
    private static final String EMAIL_SUBJECT = "Welcome to the walia cloud!  ";

    private static final String SENDER_EMAIL = System.getenv("SenderEmail");

    public Object handleRequest(SNSEvent req, Context context) {
        if (req.getRecords() == null) {

            LOG.error("No records available");
            return null;
        }

        String msgFromSQS = req.getRecords().get(0).getSNS().getMessage();
        JsonObject jsonObject = JsonParser.parseString(msgFromSQS).getAsJsonObject();

        LOG.info("messageFromSQS=" + msgFromSQS);
        String emailRecipient = (String) jsonObject.get("EmailAddress").getAsString();
        String accessToken = (String) jsonObject.get("AccessToken").getAsString();

        LOG.info("emailRecipient=" + emailRecipient);
        LOG.info("accessToken=" + accessToken);

        dynamodbClient = AmazonDynamoDBClientBuilder.defaultClient();

        DynamoDB dynamoDB = new DynamoDB(dynamodbClient);
        Table table = dynamoDB.getTable("token_sent_email");
        LOG.info("connected to token sent table");
        Item item = table.getItem("id", emailRecipient);
        LOG.info("item=" + item);
        if (item != null)
            return null;

        String emailBody = "Thanks for using WayUp!\n To verify that this is your email address, please click on link below into your browser, and you'll be sent to a page where you can get started..\n";
        emailBody += "http://walia-cloud.me/v1/verifyUserEmail?email=" + emailRecipient + "&token="
                + accessToken;

        // emailBody += "We're so excited to have you!\nSincerely\nwalia cloud";
        Content content = new Content().withData(emailBody);
        Body body = new Body().withText(content);
        try {
            LOG.info("Before AmazonSimpleEmailService");
            AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
                    .withRegion("us-east-1").build();
            LOG.info("Before SendEmailRequest");
            SendEmailRequest emailRequest = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(emailRecipient))
                    .withMessage(new Message()
                            .withBody(body)
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(EMAIL_SUBJECT)))
                    .withSource(SENDER_EMAIL);
            client.sendEmail(emailRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        item = new Item().withPrimaryKey("id", emailRecipient);
        LOG.info("before item put");
        table.putItem(item);
        LOG.info("item put successfully");
        return null;

    }
}
