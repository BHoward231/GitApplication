package com.github.bhoward231.gitapplication.tools;

import com.github.bhoward231.gitapplication.Main;
import javafx.scene.control.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocalTools {
    public static void fillListWithBranches(ListView<String> list) {
        list.getItems().clear();

        try (Repository repository = Main.getWorkingRepo()) {
            try (Git git = new Git(repository)) {
                git.branchList()
                        .call()
                        .forEach(branch -> list.getItems().add(branch.getName()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void createNewBranch(String branchName) {
        try(Repository repository = Main.getWorkingRepo()) {
            try(Git git = new Git(repository)) {
                git.branchCreate()
                        .setName(branchName)
                        .call();
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
        }
    }

    public static void checkoutBranch(String branchName) {
        try(Git git = new Git(Main.getWorkingRepo())) {
            git.checkout()
                    .setName(branchName)
                    .call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void mergeBranch(ListView<String> branchOptions) {
        List<String> choices = new ArrayList<>(branchOptions.getItems());

        ChoiceDialog<String> dialog = new ChoiceDialog<>("master", choices);
        dialog.setTitle("Merge");
        dialog.setHeaderText("Select Branch to merge to");
        dialog.setContentText("Choose Branch:");

        Optional<String> option = dialog.showAndWait();

        try(Git git = new Git(Main.getWorkingRepo())) {
            List<Ref> branches = git.branchList().call();
            branches.forEach(branch -> {
                if(branch.getName().equals(option.get())) {
                    try {
                        git.merge()
                                .include(branch)
                                .setCommit(true)
                                .call();

                        System.out.println("Merged " + git.getRepository().getBranch() + " into " + branch.getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void scanCommitList(Ref branch, ListView<String> commitList) {
        commitList.getItems().clear();

        try(Repository repository = Main.getWorkingRepo()) {
            try (Git git = new Git(repository)) {
                Iterable<RevCommit> commits = git.log().add(Main.getWorkingRepo().resolve(branch.getName())).call();
                commits.forEach(commit -> commitList.getItems().add(commit.getName()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteBranch(String branchName) {
        try(Repository repository = Main.getWorkingRepo()) {
            try(Git git = new Git(repository)) {
                git.branchDelete()
                        .setBranchNames(branchName)
                        .setForce(true)
                        .call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
