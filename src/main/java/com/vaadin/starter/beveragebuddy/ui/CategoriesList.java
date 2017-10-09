/*
 * Copyright 2000-2017 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.starter.beveragebuddy.ui;

import java.util.List;

import com.vaadin.router.Route;
import com.vaadin.router.Title;
import com.vaadin.starter.beveragebuddy.backend.Category;
import com.vaadin.starter.beveragebuddy.backend.CategoryService;
import com.vaadin.starter.beveragebuddy.backend.Review;
import com.vaadin.starter.beveragebuddy.backend.ReviewService;
import com.vaadin.ui.button.Button;
import com.vaadin.ui.common.HasValue;
import com.vaadin.ui.grid.Grid;
import com.vaadin.ui.html.Div;
import com.vaadin.ui.icon.Icon;
import com.vaadin.ui.icon.VaadinIcons;
import com.vaadin.ui.textfield.TextField;

/**
 * Displays the list of available categories, with a search filter as well as
 * buttons to add a new category or edit existing ones.
 */
@Route(value = "categories", layout = MainLayout.class)
@Title("Categories List")
public class CategoriesList extends Div {

    private final TextField searchField = new TextField("", "Search");
    private final Grid<Category> grid = new Grid<>();

    private final CategoryEditorDialog form = new CategoryEditorDialog(
            this::saveCategory, this::deleteCategory);

    private final PaperToast notification = new PaperToast();

    public CategoriesList() {
        initView();

        addSearchBar();
        addGrid();

        updateView();
    }

    private void initView() {
        addClassName("categories-list");

        notification.addClassName("notification");
        add(notification, form);
    }

    private void addSearchBar() {
        Div viewToolbar = new Div();
        viewToolbar.addClassName("view-toolbar");

        searchField.addToPrefix(new Icon(VaadinIcons.SEARCH));
        searchField.addClassName("view-toolbar__search-field");
        searchField.addValueChangeListener(e -> updateView());

        Button newButton = new Button("New category",
                new Icon(VaadinIcons.PLUS));
        newButton.getElement().setAttribute("theme", "primary");
        newButton.addClassName("view-toolbar__button");
        newButton.addClickListener(e -> form.open(new Category(),
                AbstractEditorDialog.Operation.ADD));

        viewToolbar.add(searchField, newButton);
        add(viewToolbar);
    }

    private void addGrid() {
        grid.addColumn("Category", Category::getName);
        grid.addColumn("Beverages", this::getReviewCount);
        // Grid does not yet implement HasStyle
        grid.getElement().getClassList().add("categories");
        grid.getElement().setAttribute("theme", "row-dividers");
        grid.asSingleSelect().addValueChangeListener(this::selectionChanged);
        add(grid);
    }

    private void selectionChanged(
            HasValue.ValueChangeEvent<Grid<Category>, Category> event) {
        Category selectedItem = event.getValue();

        if (selectedItem != null) {
            form.open(selectedItem, AbstractEditorDialog.Operation.EDIT);
            grid.getSelectionModel().deselect(selectedItem);
        }
    }

    private String getReviewCount(Category category) {
        List<Review> reviewsInCategory = ReviewService.getInstance()
                .findReviews(category.getName());
        int sum = reviewsInCategory.stream().mapToInt(Review::getCount).sum();
        return Integer.toString(sum);
    }

    private void updateView() {
        List<Category> categories = CategoryService.getInstance()
                .findCategories(searchField.getValue());
        grid.setItems(categories);
    }

    private void saveCategory(Category category,
            AbstractEditorDialog.Operation operation) {
        CategoryService.getInstance().saveCategory(category);

        notification.show(
                "Category successfully " + operation.getNameInText() + "ed.");
        updateView();
    }

    private void deleteCategory(Category category) {
        List<Review> reviewsInCategory = ReviewService.getInstance()
                .findReviews(category.getName());

        reviewsInCategory.forEach(review -> {
            review.setCategory(null);
            ReviewService.getInstance().saveReview(review);
        });
        CategoryService.getInstance().deleteCategory(category);

        notification.show("Category successfully deleted.");
        updateView();
    }
}
