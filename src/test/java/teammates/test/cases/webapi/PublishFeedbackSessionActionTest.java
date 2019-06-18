package teammates.test.cases.webapi;

import org.apache.http.HttpStatus;
import org.testng.annotations.Test;

import teammates.common.datatransfer.attributes.CourseAttributes;
import teammates.common.datatransfer.attributes.FeedbackSessionAttributes;
import teammates.common.util.Const;
import teammates.ui.webapi.action.JsonResult;
import teammates.ui.webapi.action.PublishFeedbackSessionAction;
import teammates.ui.webapi.output.FeedbackSessionData;
import teammates.ui.webapi.output.FeedbackSessionPublishStatus;
import teammates.ui.webapi.output.MessageOutput;

/**
 * SUT: {@link PublishFeedbackSessionAction}.
 */
public class PublishFeedbackSessionActionTest extends BaseActionTest<PublishFeedbackSessionAction> {

    @Override
    protected String getActionUri() {
        return Const.ResourceURIs.SESSION_PUBLISH;
    }

    @Override
    protected String getRequestMethod() {
        return POST;
    }

    @Test
    @Override
    protected void testExecute() throws Exception {
        ______TS("Typical case");

        CourseAttributes course = typicalBundle.courses.get("typicalCourse1");
        FeedbackSessionAttributes session = typicalBundle.feedbackSessions.get("session1InCourse1");

        String[] params = {
                Const.ParamsNames.COURSE_ID, course.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session.getFeedbackSessionName(),
        };

        PublishFeedbackSessionAction publishFeedbackSessionAction = getAction(params);

        JsonResult result = getJsonResult(publishFeedbackSessionAction);
        FeedbackSessionData feedbackSessionData = (FeedbackSessionData) result.getOutput();

        assertEquals(feedbackSessionData.getFeedbackSessionName(), session.getFeedbackSessionName());
        assertEquals(feedbackSessionData.getPublishStatus(), FeedbackSessionPublishStatus.PUBLISHED);
        assertEquals(logic.getFeedbackSession(
                session.getFeedbackSessionName(), course.getId()), FeedbackSessionPublishStatus.PUBLISHED);

        ______TS("Failure case: Session is already published");
        // Attempt to publish the same session again.
        assertTrue(logic.getFeedbackSession(session.getFeedbackSessionName(), course.getId()).isPublished());

        result = getJsonResult(getAction(params));
        MessageOutput output = (MessageOutput) result.getOutput();

        assertEquals(output.getMessage(), "Error publishing feedback session: Session has already been published.");
        assertEquals(result.getStatusCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testExecute_invalidRequests_shouldFail() {
        ______TS("non existent session name");

        CourseAttributes course = typicalBundle.courses.get("typicalCourse1");
        FeedbackSessionAttributes session = typicalBundle.feedbackSessions.get("session1InCourse1");
        String randomSessionName = "randomName";

        assertNotNull(logic.getFeedbackSession(session.getFeedbackSessionName(), course.getId()));

        String[] params = {
                Const.ParamsNames.COURSE_ID, course.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, randomSessionName,
        };

        assertNull(logic.getFeedbackSession(randomSessionName, course.getId()));

        PublishFeedbackSessionAction publishFeedbackSessionAction = getAction(params);
        JsonResult result = getJsonResult(publishFeedbackSessionAction);
        MessageOutput output = (MessageOutput) result.getOutput();

        assertEquals(String.format("Trying to update a non-existent feedback session: %s/%s",
                course.getId(), randomSessionName), output.getMessage());
        assertEquals(result.getStatusCode(), HttpStatus.SC_INTERNAL_SERVER_ERROR);

        ______TS("non existent course id");

        String randomCourseId = "randomCourseId";

        params = new String[] {
                Const.ParamsNames.COURSE_ID, randomCourseId,
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session.getFeedbackSessionName(),
        };
        assertNull(logic.getFeedbackSession(session.getFeedbackSessionName(), randomCourseId));

        publishFeedbackSessionAction = getAction(params);
        result = getJsonResult(publishFeedbackSessionAction);
        output = (MessageOutput) result.getOutput();

        assertEquals(String.format("Trying to update a non-existent feedback session: %s/%s",
                randomCourseId, session.getFeedbackSessionName()), output.getMessage());
        assertEquals(result.getStatusCode(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    @Override
    protected void testAccessControl() throws Exception {
        CourseAttributes course = typicalBundle.courses.get("typicalCourse1");
        FeedbackSessionAttributes session = typicalBundle.feedbackSessions.get("session1InCourse1");

        String[] submissionParams = {
                Const.ParamsNames.COURSE_ID, course.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session.getFeedbackSessionName(),
        };

        verifyOnlyInstructorsOfTheSameCourseCanAccess(submissionParams);
        verifyInaccessibleWithoutModifyCoursePrivilege(submissionParams);
    }
}
