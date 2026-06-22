package com.ms.email.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_EMAILS")
public class EmailModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emailId;
    
    private Long userId;
    private String emailFrom;
    private String emailTo;
    private String subject;
    
    @Column(columnDefinition = "TEXT") 
    private String text;
    
    private LocalDateTime sendDateEmail;
    private String status;

    // Getters e Setters
    public Long getEmailId() { return emailId; }
    public void setEmailId(Long emailId) { this.emailId = emailId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmailFrom() { return emailFrom; }
    public void setEmailFrom(String emailFrom) { this.emailFrom = emailFrom; }

    public String getEmailTo() { return emailTo; }
    public void setEmailTo(String emailTo) { this.emailTo = emailTo; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDateTime getSendDateEmail() { return sendDateEmail; }
    public void setSendDateEmail(LocalDateTime sendDateEmail) { this.sendDateEmail = sendDateEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}