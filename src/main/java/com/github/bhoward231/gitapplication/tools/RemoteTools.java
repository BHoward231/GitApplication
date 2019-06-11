package com.github.bhoward231.gitapplication.tools;

import com.github.bhoward231.gitapplication.Main;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.util.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.util.Optional;

public class RemoteTools {
    public static void pushToRemote() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
        dialog.setHeaderText("Look, a Custom Login Dialog");

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(username::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> keyPair = dialog.showAndWait();

        TextInputDialog repoNameDialog = new TextInputDialog();
        repoNameDialog.setTitle("Enter Name");
        repoNameDialog.setContentText("URL to Repo");

        Optional<String> repositoryUrl = repoNameDialog.showAndWait();

        try(Git git = new Git(Main.getWorkingRepo())) {
            git.remoteAdd()
                    .setName("origin")
                    .setUri(new URIish(repositoryUrl.get()))
                    .call();

            PushCommand pushCommand = git.push()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(keyPair.get().getKey(), keyPair.get().getValue()));

            pushCommand.call();

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText("Finished Pushing Repo");

            a.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cloneRepo(MenuItem anchor) {
        TextInputDialog urlPrompt = new TextInputDialog();
        urlPrompt.setTitle("Enter URL");
        urlPrompt.setContentText("Enter the URL to clone from");

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Enter Location to clone to");

        Optional<String> url = urlPrompt.showAndWait();
        File directory = directoryChooser.showDialog(anchor.getParentPopup().getOwnerWindow());

        url.ifPresent(address -> {
            try {
                Git.cloneRepository()
                        .setURI(address)
                        .setDirectory(directory)
                        .call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText("Finished Cloning Repo");

        a.show();
    }
}
