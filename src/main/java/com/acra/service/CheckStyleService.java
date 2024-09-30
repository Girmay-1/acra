package com.acra.service;

import com.acra.model.Issue;
import com.acra.model.IssueSeverity;
import com.acra.model.IssueType;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CheckStyleService {
    private static Logger logger = LoggerFactory.getLogger(CheckStyleService.class);
    private final Configuration checkstyleConfig;

    public CheckStyleService(@Value("${checkstyle.config}") String checkstyleConfigPath) throws CheckstyleException {
        this.checkstyleConfig = ConfigurationLoader.loadConfiguration(
                checkstyleConfigPath,
                new PropertiesExpander(System.getProperties())
        );
    }

    public List<Issue> analyzeJavaFile(String filePath, String fileContent) {
        List<Issue> issues = new ArrayList<>();

        Checker checker = new Checker();
        try {
            checker.setModuleClassLoader(Checker.class.getClassLoader());
            checker.configure(checkstyleConfig);

            checker.addListener(new AuditListener() {
                @Override
                public void auditStarted(AuditEvent evt) {
                    // No action needed at start of audit
                }

                @Override
                public void auditFinished(AuditEvent evt) {
                    // No action needed at end of audit
                }

                @Override
                public void fileStarted(AuditEvent evt) {
                    // No action needed at start of file
                }

                @Override
                public void fileFinished(AuditEvent evt) {
                    // No action needed at end of file
                }

                @Override
                public void addError(AuditEvent evt) {
                    Issue issue = new Issue();
                    issue.setFile(filePath);
                    issue.setLine(evt.getLine());
                    issue.setMessage(evt.getMessage());
                    issue.setType(IssueType.CODE_STYLE); // Assuming all Checkstyle issues are CODE_STYLE
                    issue.setSeverity(mapSeverity(evt.getSeverityLevel()));
                    issues.add(issue);
                }

                @Override
                public void addException(AuditEvent evt, Throwable throwable) {
                    // Log exception or handle as needed
                }
            });

            // Create a temporary file with the content
            File tempFile = File.createTempFile("checkstyle-", ".java");
            java.nio.file.Files.write(tempFile.toPath(), fileContent.getBytes());

            checker.process(Collections.singletonList(tempFile));

            // Clean up the temporary file
            tempFile.delete();
        } catch (Exception e) {
            logger.error("exception in checker:", e);
        } finally {
            checker.destroy();
        }

        return issues;
    }

    private IssueSeverity mapSeverity(SeverityLevel checkstyleSeverity) {
        switch (checkstyleSeverity) {
            case ERROR:
                return IssueSeverity.HIGH;
            case WARNING:
                return IssueSeverity.MEDIUM;
            case INFO:
                return IssueSeverity.LOW;
            default:
                return IssueSeverity.LOW;
        }
    }
}