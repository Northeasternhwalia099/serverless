package com.csye6225.project.Serverless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class EmailEvent {

    private static final Logger logger = LoggerFactory.getLogger(EmailEvent.class);

    public Object handleRequest(SNSEvent req, Context context) {
        if (req.getRecords() == null) {

            logger.error("No records available");
            return null;
        }

        String msgFromSQS = req.getRecords().get(0).getSNS().getMessage();
        JsonObject jsonObject = JsonParser.parseString(msgFromSQS).getAsJsonObject();

        logger.info("messageFromSQS=" + msgFromSQS);
        String emailRecipient = (String) jsonObject.get("EmailAddress").getAsString();
        String accessToken = (String) jsonObject.get("AccessToken").getAsString();

        logger.info("emailRecipient=" + emailRecipient);
        logger.info("accessToken=" + accessToken);
        return accessToken;

    }
}
