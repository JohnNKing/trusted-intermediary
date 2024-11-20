package gov.hhs.cdc.trustedintermediary.etor.messages;

import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataOrchestrator;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.Set;
import javax.inject.Inject;

public class SendMessageHelper {
    private static final SendMessageHelper INSTANCE = new SendMessageHelper();

    public static SendMessageHelper getInstance() {
        return INSTANCE;
    }

    @Inject Logger logger;

    @Inject PartnerMetadataOrchestrator partnerMetadataOrchestrator;

    private SendMessageHelper() {}

    public void savePartnerMetadataForReceivedMessage(PartnerMetadata partnerMetadata) {
        if (partnerMetadata.inboundReportId() == null) {
            logger.logWarning(
                    "Inbound reportId is null so not saving metadata for received message");
            return;
        }
        try {
            partnerMetadataOrchestrator.updateMetadataForReceivedMessage(partnerMetadata);
        } catch (PartnerMetadataException e) {
            logger.logError(
                    "Unable to save metadata for inboundReportId "
                            + partnerMetadata.inboundReportId(),
                    e);
        }
    }

    public void saveSentMessageSubmissionId(String inboundReportId, String sentSubmissionId) {
        if (sentSubmissionId == null || inboundReportId == null) {
            logger.logWarning(
                    "Inbound and/or sent reportId is null so not saving metadata for sent result");
            return;
        }

        try {
            partnerMetadataOrchestrator.updateMetadataForSentMessage(
                    inboundReportId, sentSubmissionId);
        } catch (PartnerMetadataException e) {
            logger.logError(
                    "Unable to update metadata for inbound reportId "
                            + inboundReportId
                            + " and sent submissionId "
                            + sentSubmissionId,
                    e);
        }
    }

    public void linkMessage(String inboundReportId) {
        if (inboundReportId == null) {
            logger.logWarning("Inbound reportId is null so not linking messages");
            return;
        }

        try {
            Set<String> messageIdsToLink =
                    partnerMetadataOrchestrator.findMessagesIdsToLink(inboundReportId);

            if (messageIdsToLink == null || messageIdsToLink.isEmpty()) {
                return;
            }

            // Add inboundReportId to complete the list of messageIds to link
            messageIdsToLink.add(inboundReportId);

            logger.logInfo(
                    "Found messages to link for inboundReportId {}: {}",
                    inboundReportId,
                    messageIdsToLink);
            partnerMetadataOrchestrator.linkMessages(messageIdsToLink);
        } catch (PartnerMetadataException | MessageLinkException e) {
            logger.logError("Unable to link messages for inbound reportId " + inboundReportId, e);
        }
    }
}
