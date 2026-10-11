package com.company.bridge.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AlertEvent(

        String EventType,
        String Source,
        String CANId,
        String State,
        String APMAlertId,
        String Category,
        String Subcategory,
        String AlertType,
        String AlertSubcategory,
        String Severity,
        String IPAddress,
        String ShortDescription,
        String Description,
        String MetricValue,
        String AlertStartTimeStamp,
        String AlertEndTimeStamp,
        String ResolutionCode,

        String OptionalField1,
        String OptionalField2,
        String OptionalField3,
        String OptionalField4,
        String OptionalField5,
        String OptionalField6,
        String OptionalField7,
        String OptionalField8,
        String OptionalField9,
        String OptionalField10,
        String OptionalField11,

        @JsonProperty("Attachment_Name") String attachmentName,

        @JsonProperty("Attachment_Type") String attachmentType,

        @JsonProperty("Attachment_File") String attachmentFile) {
}