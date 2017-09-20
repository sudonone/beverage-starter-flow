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
import java.util.Optional;

import com.vaadin.annotations.Convert;
import com.vaadin.annotations.EventHandler;
import com.vaadin.annotations.HtmlImport;
import com.vaadin.annotations.Id;
import com.vaadin.annotations.ModelItem;
import com.vaadin.annotations.Route;
import com.vaadin.annotations.Tag;
import com.vaadin.annotations.Title;
import com.vaadin.flow.router.View;
import com.vaadin.flow.template.PolymerTemplate;
import com.vaadin.flow.template.model.TemplateModel;
import com.vaadin.starter.beveragebuddy.backend.Review;
import com.vaadin.starter.beveragebuddy.backend.ReviewService;
import com.vaadin.starter.beveragebuddy.ui.ReviewsList.ReviewsModel;
import com.vaadin.starter.beveragebuddy.ui.converters.LocalDateToStringConverter;
import com.vaadin.starter.beveragebuddy.ui.converters.LongToStringConverter;
import com.vaadin.ui.AttachEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;

/**
 * Simple template example.
 */
@Route(value = "", layout = MainLayout.class)
@Title("Review List")
@Tag("reviews-list")
@HtmlImport("frontend://ReviewsList.html")
public class ReviewsList extends PolymerTemplate<ReviewsModel> implements View {

    public static interface ReviewsModel extends TemplateModel {
        @Convert(value = LongToStringConverter.class, path = "id")
        @Convert(value = LocalDateToStringConverter.class, path = "date")
        @Convert(value = LongToStringConverter.class, path = "category.id")
        void setReviews(List<Review> reviews);
    }

    @Id("filterText")
    private TextField filterText;
    @Id("addReview")
    private Button addReview;
    @Id("notification")
    private PaperToast notification;

    private ReviewForm reviewForm = new ReviewForm(this::saveUpdate,
            this::deleteUpdate);

    public ReviewsList() {
        filterText.setPlaceholder("Find a review...");
        filterText.addValueChangeListener(e -> updateList());

        addReview.setText("Add new review");
        addReview.addClickListener(e -> addReviewClicked());
        updateList();

    }

    public void saveUpdate(Review review) {
        ReviewService.getInstance().saveReview(review);
        updateList();
        notification.show("A new review/edit has been saved.");
    }

    public void deleteUpdate(Review review) {
        ReviewService.getInstance().deleteReview(review);
        updateList();
        notification.show("Your selected review has been deleted.");
    }

    private void updateList() {
        getModel().setReviews(
                ReviewService.getInstance().findReviews(filterText.getValue()));
    }

    @EventHandler
    private void edit(@ModelItem Review review) {
        reviewForm.openReview(Optional.of(review));
    }

    private void addReviewClicked() {
        reviewForm.openReview(Optional.empty());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        getElement().getParent().appendChild(reviewForm.getElement());
    }

}