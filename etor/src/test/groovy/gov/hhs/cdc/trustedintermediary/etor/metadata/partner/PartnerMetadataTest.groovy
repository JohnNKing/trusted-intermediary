package gov.hhs.cdc.trustedintermediary.etor.metadata.partner

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType

import java.time.Instant
import spock.lang.Specification

class PartnerMetadataTest extends Specification {
    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(PartnerMetadata)

        then:
        noExceptionThrown()
    }

    def "test constructor"() {
        given:
        def inboundReportId = "inboundReportId"
        def outboundReportId = "outboundReportId"
        def timeReceived = Instant.now()
        def timeDelivered = Instant.now()
        def hash = "abcd"
        def status = PartnerMetadataStatus.DELIVERED
        def failureReason = "failure reason"
        def messageType = PartnerMetadataMessageType.RESULT
        def sendingAppDetails = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacilityDetails = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingAppDetails = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacilityDetails = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")

        when:
        def metadata = new PartnerMetadata(inboundReportId, outboundReportId, timeReceived, timeDelivered, hash, PartnerMetadataStatus.DELIVERED, failureReason, messageType, sendingAppDetails, sendingFacilityDetails, receivingAppDetails, receivingFacilityDetails, "placer_order_number")

        then:
        metadata.inboundReportId() == inboundReportId
        metadata.outboundReportId() == outboundReportId
        metadata.timeDelivered() == timeDelivered
        metadata.timeReceived() == timeReceived
        metadata.hash() == hash
        metadata.deliveryStatus() == status
        metadata.failureReason() == failureReason
        metadata.sendingApplicationDetails() == sendingAppDetails
        metadata.sendingFacilityDetails() == sendingFacilityDetails
        metadata.receivingApplicationDetails() == receivingAppDetails
        metadata.receivingFacilityDetails() == receivingFacilityDetails
    }

    def "test constructor with only inbound message ID and status"() {
        given:
        def inboundReportId = "inboundReportId"
        def deliverStatus = PartnerMetadataStatus.DELIVERED

        when:
        def metadata = new PartnerMetadata(inboundReportId, deliverStatus)

        then:
        metadata.inboundReportId() == inboundReportId
        metadata.outboundReportId() == null
        metadata.timeReceived() == null
        metadata.timeDelivered() == null
        metadata.hash() == null
        metadata.deliveryStatus() == deliverStatus
    }

    def 'test withOutboundReportId to update PartnerMetadata'() {
        given:
        def inboundReportId = "inboundReportId"
        def outboundReportId = "outboundReportId"
        def messageType = PartnerMetadataMessageType.RESULT
        def timeReceived = Instant.now()
        def hash = "abcd"
        def status = PartnerMetadataStatus.DELIVERED
        def failureReason = "DogCow goes boom"
        def sendingAppDetails = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacilityDetails = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingAppDetails = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacilityDetails = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")

        def metadata = new PartnerMetadata(inboundReportId, null, timeReceived, null, hash, status, failureReason, messageType, sendingAppDetails, sendingFacilityDetails, receivingAppDetails, receivingFacilityDetails, "placer_order_number")

        when:
        def updatedMetadata = metadata.withOutboundReportId(outboundReportId)

        then:
        updatedMetadata.inboundReportId() == inboundReportId
        updatedMetadata.outboundReportId() == outboundReportId
        updatedMetadata.timeReceived() == timeReceived
        updatedMetadata.hash() == hash
        updatedMetadata.deliveryStatus() == status
        updatedMetadata.sendingApplicationDetails() == sendingAppDetails
        updatedMetadata.sendingFacilityDetails() == sendingFacilityDetails
        updatedMetadata.receivingApplicationDetails() == receivingAppDetails
        updatedMetadata.receivingFacilityDetails() == receivingFacilityDetails
    }

    def "test withDeliveryStatus to update PartnerMetadata"() {
        given:
        def inboundReportId = "inboundReportId"
        def outboundReportId = "outboundReportId"
        def timeReceived = Instant.now()
        def timeDelivered = null
        def messageType = PartnerMetadataMessageType.RESULT
        def hash = "abcd"
        def failureReason = "DogCow goes boom"
        def sendingAppDetails = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacilityDetails = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingAppDetails = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacilityDetails = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def metadata = new PartnerMetadata(inboundReportId, outboundReportId, timeReceived, timeDelivered, hash, PartnerMetadataStatus.PENDING, failureReason, messageType, sendingAppDetails, sendingFacilityDetails, receivingAppDetails, receivingFacilityDetails, "placer_order_number")

        when:
        def newStatus = PartnerMetadataStatus.DELIVERED
        def updatedMetadata = metadata.withOutboundReportId(outboundReportId).withDeliveryStatus(newStatus)

        then:
        updatedMetadata.inboundReportId() == inboundReportId
        updatedMetadata.outboundReportId() == outboundReportId
        updatedMetadata.timeReceived() == timeReceived
        updatedMetadata.timeDelivered() == null
        updatedMetadata.hash() == hash
        updatedMetadata.deliveryStatus() == newStatus
        updatedMetadata.failureReason() == failureReason
        updatedMetadata.sendingApplicationDetails() == sendingAppDetails
        updatedMetadata.sendingFacilityDetails() == sendingFacilityDetails
        updatedMetadata.receivingApplicationDetails() == receivingAppDetails
        updatedMetadata.receivingFacilityDetails() == receivingFacilityDetails
    }

    def "test withTimeDelivered to update PartnerMetadata"() {
        given:
        def inboundReportId = "inboundReportId"
        def outboundReportId = "outboundReportId"
        def timeReceived = Instant.now()
        def timeDelivered = Instant.now()
        def messageType = PartnerMetadataMessageType.RESULT
        def hash = "abcd"
        def failureReason = "DogCow goes boom"
        def sendingAppDetails = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacilityDetails = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingAppDetails = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacilityDetails = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def metadata = new PartnerMetadata(inboundReportId, outboundReportId, timeReceived, null, hash, PartnerMetadataStatus.PENDING, failureReason, messageType, sendingAppDetails, sendingFacilityDetails, receivingAppDetails, receivingFacilityDetails, "placer_order_number")

        when:
        def updatedMetadata = metadata.withTimeDelivered(timeDelivered)

        then:
        updatedMetadata.inboundReportId() == inboundReportId
        updatedMetadata.outboundReportId() == outboundReportId
        updatedMetadata.timeReceived() == timeReceived
        updatedMetadata.timeDelivered() == timeDelivered
        updatedMetadata.hash() == hash
        updatedMetadata.failureReason() == failureReason
        updatedMetadata.sendingApplicationDetails() == sendingAppDetails
        updatedMetadata.sendingFacilityDetails() == sendingFacilityDetails
        updatedMetadata.receivingApplicationDetails() == receivingAppDetails
        updatedMetadata.receivingFacilityDetails() == receivingFacilityDetails
    }
}
