package com.github.bhoward231.gitapplication;

import com.github.bhoward231.gitapplication.tools.LocalTools;
import com.github.bhoward231.gitapplication.tools.RemoteTools;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable {
    public Button btnOpenRepo;
    public Button btnCommit;
    public Button btnStage;
    public Button btnRefresh;
    public Button btnUnstage;

    public MenuItem newRepoOption;
    public MenuItem newBranchOption;
    public MenuItem cloneOption;
    public MenuItem pushOption;

    public TextField txtWorkingDirectory;
    public TextField txtCommitMessage;

    public ListView<String> lstBranches;
    public ListView<String> lstUnstagedChanges;
    public ListView<String> lstStagedChanges;
    public ListView<String> lstCommitsList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    public void openRepo() {
        DirectoryChooser chooseDirectory = new DirectoryChooser();
        chooseDirectory.setTitle("Open Repo");

        try(Repository repository = new RepositoryBuilder()
                .setWorkTree(chooseDirectory.showDialog(btnOpenRepo
                        .getScene()
                        .getWindow()))
                .build()) {

            Main.setWorkingRepo(repository);
            LocalTools.fillListWithBranches(lstBranches);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void createNewBranch() {
        TextInputDialog dialog = new TextInputDialog("New Branch");
        dialog.setTitle("Name of Branch");
        dialog.setHeaderText("Please Enter a branch name");
        dialog.setContentText("Name of Branch:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(LocalTools::createNewBranch);

        LocalTools.fillListWithBranches(lstBranches);
    }

    @FXML
    public void checkoutBranch() {
        LocalTools.checkoutBranch(lstBranches.getSelectionModel().getSelectedItem());
        try {
            LocalTools.scanCommitList(Main.getWorkingRepo().findRef(lstBranches.getSelectionModel().getSelectedItem()), lstCommitsList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void createNewRepo() {
        DirectoryChooser chooseLocation = new DirectoryChooser();
        chooseLocation.setTitle("Choose Location");
        Main.initializeGitRepository(chooseLocation.showDialog(txtWorkingDirectory.getScene().getWindow()));

        LocalTools.fillListWithBranches(lstBranches);

        txtWorkingDirectory.setText(Main.getWorkingRepo().getDirectory().getAbsolutePath());
    }

    @FXML
    public void commitFiles() {
        try (Git git = new Git(Main.getWorkingRepo())) {
            git.commit()
                    .setMessage(txtCommitMessage.getText())
                    .call();
        } catch (Exception e) {
            e.printStackTrace();
        }

        refreshChanges();
    }

    @FXML
    public void stageFile() {
        try(Git git = new Git(Main.getWorkingRepo())) {
            git.add()
                    .addFilepattern(lstUnstagedChanges.getSelectionModel().getSelectedItem())
                    .call();
        } catch (NoFilepatternException e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Please select a file to stage");
        } catch (Exception e) {
            e.printStackTrace();
        }

        refreshChanges();
    }

    @FXML
    public void unstageFile() {
        try(Git git = new Git(Main.getWorkingRepo())) {
            git.getRepository().readDirCache().clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

        refreshChanges();

    }

    @FXML
    public void refreshChanges() {
        lstUnstagedChanges.getItems().clear();
        lstStagedChanges.getItems().clear();

        try (Git git = new Git(Main.getWorkingRepo())) {
            Status status = git.status().call();
            status.getUntracked().forEach(lstUnstagedChanges.getItems()::add);
            status.getUncommittedChanges().forEach(lstStagedChanges.getItems()::add);
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void deleteBranch() {
        List<String> choices = new ArrayList<>(lstBranches.getItems());

        ChoiceDialog<String> dialog = new ChoiceDialog<>("master", choices);
        dialog.setTitle("Delete");
        dialog.setHeaderText("Select Branch to Delete");
        dialog.setContentText("Choose Branch:");

        Optional<String> option = dialog.showAndWait();

        option.ifPresent(LocalTools::deleteBranch);

        LocalTools.fillListWithBranches(lstBranches);
    }

    @FXML
    public void mergeBranch() {
        LocalTools.mergeBranch(lstBranches);
    }

    @FXML
    public void cloneRepo() {
        RemoteTools.cloneRepo(cloneOption);
    }

    @FXML
    public void pushToGithub() {
        RemoteTools.pushToRemote();
    }
}
