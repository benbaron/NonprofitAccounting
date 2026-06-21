package org.nonprofitbookkeeping.ui;

import java.util.Collection;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Shared layout scaffold for native alternate UI panels.
 */
public class AlternatePanelScaffold extends BorderPane
{
    private final Label titleLabel = new Label();
    private final Label subtitleLabel = new Label();
    private final HBox primaryActions = new HBox(8);
    private final HBox secondaryActions = new HBox(8);
    private final VBox header = new VBox(8);
    private final VBox bannerBox = new VBox(6);
    private final StackPane contentStack = new StackPane();
    private final Label emptyState = new Label();
    private final Label loadingState = new Label("Loading...");
    private final Label errorState = new Label();
    private final Label warningBanner = new Label();
    private final Label footer = new Label();

    private Node content;
    private Node filterBar;
    private Node footerNode = this.footer;

    public AlternatePanelScaffold(String title)
    {
        getStyleClass().add("alternate-panel-scaffold");
        this.titleLabel.setText(title);
        this.titleLabel.getStyleClass().addAll("alternate-panel-title", "panel-title");
        this.subtitleLabel.getStyleClass().add("alternate-panel-subtitle");
        this.subtitleLabel.setWrapText(true);
        this.emptyState.getStyleClass().addAll("alternate-panel-state", "alternate-panel-empty-state");
        this.emptyState.setWrapText(true);
        this.loadingState.getStyleClass().addAll("alternate-panel-state", "alternate-panel-loading-state");
        this.errorState.getStyleClass().addAll("alternate-panel-state", "alternate-panel-error-state", "validation-error");
        this.errorState.setWrapText(true);
        this.warningBanner.getStyleClass().addAll("alternate-panel-banner", "alternate-panel-warning-banner", "state-warning");
        this.warningBanner.setWrapText(true);
        this.footer.getStyleClass().add("alternate-panel-footer");
        this.primaryActions.getStyleClass().add("alternate-panel-primary-actions");
        this.secondaryActions.getStyleClass().add("alternate-panel-secondary-actions");
        this.bannerBox.getStyleClass().add("alternate-panel-banners");
        this.header.getStyleClass().add("alternate-panel-header");
        this.contentStack.getStyleClass().add("alternate-panel-content");

        HBox actionRow = new HBox(8, this.primaryActions, spacer(), this.secondaryActions);
        actionRow.getStyleClass().add("alternate-panel-actions");
        this.header.getChildren().addAll(this.titleLabel, this.subtitleLabel, actionRow, this.bannerBox);
        setTop(this.header);
        setCenter(this.contentStack);
        setBottom(this.footerNode);
        setSubtitle(null);
        setWarningBanner(null);
        setStatus(null);
        showContent();
    }

    public void setSubtitle(String subtitle)
    {
        setLabelTextAndVisibility(this.subtitleLabel, subtitle);
    }

    public void setPrimaryActions(Collection<? extends Node> actions)
    {
        setChildren(this.primaryActions.getChildren(), actions);
    }

    public void setSecondaryActions(Collection<? extends Node> actions)
    {
        setChildren(this.secondaryActions.getChildren(), actions);
    }

    public void setFilterBar(Node filterBar)
    {
        if (this.filterBar != null)
        {
            this.header.getChildren().remove(this.filterBar);
        }
        this.filterBar = filterBar;
        if (filterBar != null)
        {
            filterBar.getStyleClass().add("alternate-panel-filter-bar");
            int bannerIndex = this.header.getChildren().indexOf(this.bannerBox);
            this.header.getChildren().add(bannerIndex, filterBar);
        }
    }

    public void setContent(Node content)
    {
        this.content = content;
        showContent();
    }

    public void setStatus(String status)
    {
        setLabelTextAndVisibility(this.footer, status);
        this.footerNode.setVisible(this.footer.isVisible());
        this.footerNode.setManaged(this.footer.isManaged());
    }

    public void setFooter(Node footerNode)
    {
        this.footerNode = footerNode == null ? this.footer : footerNode;
        this.footerNode.getStyleClass().add("alternate-panel-footer");
        setBottom(this.footerNode);
    }

    public void setWarningBanner(String warning)
    {
        setLabelTextAndVisibility(this.warningBanner, warning);
        if (!this.bannerBox.getChildren().contains(this.warningBanner))
        {
            this.bannerBox.getChildren().add(this.warningBanner);
        }
        this.bannerBox.setVisible(this.warningBanner.isVisible());
        this.bannerBox.setManaged(this.warningBanner.isManaged());
    }

    public void showContent()
    {
        setStackNode(this.content);
    }

    public void showEmpty(String message)
    {
        this.emptyState.setText(message);
        setStackNode(this.emptyState);
    }

    public void showLoading(String message)
    {
        this.loadingState.setText(message == null || message.isBlank() ? "Loading..." : message);
        setStackNode(this.loadingState);
    }

    public void showError(String message)
    {
        this.errorState.setText(message);
        setStackNode(this.errorState);
    }

    private void setStackNode(Node node)
    {
        this.contentStack.getChildren().setAll(node == null ? new Region() : node);
    }

    private static Region spacer()
    {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private static void setChildren(ObservableList<Node> target, Collection<? extends Node> children)
    {
        target.setAll(children == null ? java.util.List.of() : children);
    }

    private static void setLabelTextAndVisibility(Label label, String text)
    {
        boolean visible = text != null && !text.isBlank();
        label.setText(visible ? text : "");
        label.setVisible(visible);
        label.setManaged(visible);
    }
}
