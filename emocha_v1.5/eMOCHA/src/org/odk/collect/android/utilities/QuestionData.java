package org.odk.collect.android.utilities;

import org.odk.collect.android.widgets.IQuestionWidget;

public class QuestionData {
	IQuestionWidget questionWidget;
	int isSwipeRequired;
	
	public QuestionData(IQuestionWidget questionWidget, int isSwipeRequired) {
		this.questionWidget = questionWidget;
		this.isSwipeRequired = isSwipeRequired;
	}

	public IQuestionWidget getQuestionWidget() {
		return questionWidget;
	}

	public void setQuestionWidget(IQuestionWidget questionWidget) {
		this.questionWidget = questionWidget;
	}

	public int getIsSwipeRequired() {
		return isSwipeRequired;
	}

	public void setIsSwipeRequired(int isSwipeRequired) {
		this.isSwipeRequired = isSwipeRequired;
	}
	
	
}
