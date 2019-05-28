package teammates.test.cases.webapi;

import org.testng.annotations.Test;

import teammates.common.datatransfer.attributes.FeedbackQuestionAttributes;
import teammates.common.datatransfer.attributes.FeedbackResponseAttributes;
import teammates.common.datatransfer.attributes.StudentAttributes;
import teammates.common.exception.EntityNotFoundException;
import teammates.common.util.Const;
import teammates.ui.webapi.action.Intent;
import teammates.ui.webapi.action.UpdateFeedbackResponseAction;
import teammates.ui.webapi.request.FeedbackResponseUpdateRequest;

/**
 * SUT: {@link UpdateFeedbackResponseAction}.
 */
public class UpdateFeedbackResponseActionTest extends BaseActionTest<UpdateFeedbackResponseAction> {

    @Override
    protected String getActionUri() {
        return Const.ResourceURIs.RESPONSE;
    }

    @Override
    protected String getRequestMethod() {
        return PUT;
    }

    @Test
    @Override
    protected void testExecute() throws Exception {
        ______TS("Unsuccessful case: not enough parameters");

        verifyHttpParameterFailure();

        ______TS("Successful updated feedback response");

        int questionNumber = 1;
        FeedbackQuestionAttributes feedbackQuestion = logic.getFeedbackQuestion(
                "First feedback session", "idOfTypicalCourse1", questionNumber);

        String giverEmail = "student1InCourse1@gmail.tmt";
        String receiverEmail = "student1InCourse1@gmail.tmt";
        FeedbackResponseAttributes feedbackResponse =
                logic.getFeedbackResponse(feedbackQuestion.getId(), giverEmail, receiverEmail);

        // Edit the feedback response
        String[] submissionParams = new String[] {
                Const.ParamsNames.FEEDBACK_RESPONSE_ID, feedbackResponse.getId(),
                Const.ParamsNames.INTENT, Intent.STUDENT_SUBMISSION.toString(),
                Const.ParamsNames.FEEDBACK_SESSION_MODERATED_PERSON, giverEmail,
        };

        FeedbackResponseUpdateRequest responseUpdateRequest = new FeedbackResponseUpdateRequest();

        //TODO requestbody
        // UpdateFeedbackResponseAction action = getAction(submissionParams);
        //getJsonResult(action);
        //FeedbackResponseAttributes fra = logic.getFeedbackResponse(feedbackResponse.getId());

        // Compare before and after
        // assertEquals(fra.getResponseDetails().getAnswerString(),feedbackResponse.getResponseDetails().getAnswerString());

    }

    @Test
    @Override
    protected void testAccessControl() throws Exception {

        ______TS("non-existent feedback response");

        assertThrows(EntityNotFoundException.class, () -> {
            getAction(new String[] {Const.ParamsNames.FEEDBACK_RESPONSE_ID, "randomId"}).checkSpecificAccessControl();
        });

        ______TS("instructor can not update student feedback response");

        int questionNumber = 1;
        FeedbackQuestionAttributes feedbackQuestion = logic.getFeedbackQuestion(
                "First feedback session", "idOfTypicalCourse1", questionNumber);

        String giverEmail = "student1InCourse1@gmail.tmt";
        String receiverEmail = "student1InCourse1@gmail.tmt";
        FeedbackResponseAttributes feedbackResponse = logic.getFeedbackResponse(feedbackQuestion.getId(),
                giverEmail, receiverEmail);

        String[] params = new String[] {
                Const.ParamsNames.FEEDBACK_RESPONSE_ID, feedbackResponse.getId(),
                Const.ParamsNames.INTENT, Intent.INSTRUCTOR_SUBMISSION.toString(),
        };

        verifyInaccessibleForInstructors(params);

        ______TS("students can not access instructor feedback response");

        questionNumber = 3;
        feedbackQuestion = logic.getFeedbackQuestion(
                "First feedback session", "idOfTypicalCourse1", questionNumber);
        giverEmail = "instructor1@course1.tmt";
        receiverEmail = "%GENERAL%";
        feedbackResponse = logic.getFeedbackResponse(feedbackQuestion.getId(),
                giverEmail, receiverEmail);

        params = new String[] {
                Const.ParamsNames.FEEDBACK_RESPONSE_ID, feedbackResponse.getId(),
                Const.ParamsNames.INTENT, Intent.STUDENT_SUBMISSION.toString(),
        };

        verifyInaccessibleForStudents(params);
    }

}
