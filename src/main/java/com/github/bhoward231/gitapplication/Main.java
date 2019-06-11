package com.github.bhoward231.gitapplication;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;


import java.io.File;

public class Main extends Application {
    private static Repository workingRepo;

    public static void initializeGitRepository(File directory) {
        try (Git git = Git.init()
                .setDirectory(directory)
                .call()) {

            git.commit()
                    .setMessage("Initial Commit")
                    .call();
            workingRepo = git.getRepository();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Visual Git");
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainWindow.fxml"));
        primaryStage.setScene(new Scene(root, 640, 480));
        primaryStage.show();
    }

    public static void setWorkingRepo(Repository repo) {
        workingRepo = repo;
    }

    public static Repository getWorkingRepo() {
        return workingRepo;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
