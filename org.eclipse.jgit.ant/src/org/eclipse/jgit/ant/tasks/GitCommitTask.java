package org.eclipse.jgit.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.RawParseUtils;

import java.io.File;
import java.io.IOException;

public class GitCommitTask extends Task {

    private File src;
    
    private String author;
    
    private String message;


    public void setSrc(File src) {
        this.src = src;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void execute() throws BuildException {
        if (src == null) {
            throw new BuildException("Repository path not specified.");
        }
        if (!RepositoryCache.FileKey.isGitRepository(new File(src, ".git"),
                FS.DETECTED)) {
            throw new BuildException("Specified path (" + src
                    + ") is not a git repository.");
        }

        CommitCommand commidCmd;

        try {
            Repository repo = new FileRepositoryBuilder().readEnvironment()
                    .findGitDir(src).build();

            commidCmd = new Git(repo).commit();
        } catch (IOException e) {
            throw new BuildException("Could not access repository " + src, e);
        }

        try {
            
            if (message!=null && message.trim().length()>0) {
                handleOutput("Message : " + message);
                commidCmd.setMessage(message);
            } else {
                final String errorMessage = "Commit message is not set. Please specify parameter 'message'";
                handleErrorOutput(errorMessage);
                throw new BuildException(errorMessage);
            }

            if (author!=null && author.trim().length() > 0) {
                commidCmd.setAuthor(RawParseUtils.parsePersonIdent(author));
            }

            //Ref head = repo.getRef(Constants.HEAD);
            final RevCommit revCommit = commidCmd.call();

            //handleOutput("revCommit " + revCommit);
            //handleOutput("head " + head);
        } catch (Exception e) {
            throw new BuildException("Could not commit files. " + src, e);
        }

    }
}
