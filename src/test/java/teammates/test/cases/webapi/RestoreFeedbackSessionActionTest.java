package teammates.test.cases.webapi;

import org.testng.annotations.Test;

import teammates.common.datatransfer.attributes.CourseAttributes;
import teammates.common.datatransfer.attributes.FeedbackSessionAttributes;
import teammates.common.exception.EntityNotFoundException;
import teammates.common.util.Const;
import teammates.ui.webapi.action.JsonResult;
import teammates.ui.webapi.action.RestoreFeedbackSessionAction;
import teammates.ui.webapi.output.FeedbackSessionData;

/**
 * SUT: {@link RestoreFeedbackSessionAction}.
 */
public class RestoreFeedbackSessionActionTest extends BaseActionTest<RestoreFeedbackSessionAction> {

    @Override
    protected String getActionUri() {
        return Const.ResourceURIs.BIN_SESSION;
    }

    @Override
    protected String getRequestMethod() {
        return DELETE;
    }

    @Test
    @Override
    protected void testExecute() throws Exception {

        ______TS("Failure case: Session is not in recycle bin");

        CourseAttributes course = typicalBundle.courses.get("typicalCourse1");
        FeedbackSessionAttributes fs = typicalBundle.feedbackSessions.get("session1InCourse1");

        String[] params = new String[] {
                Const.ParamsNames.COURSE_ID, course.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fs.getFeedbackSessionName(),
        };

        assertThrows(EntityNotFoundException.class, () -> getJsonResult(getAction(params)));

        ______TS("Typical case");

        logic.moveFeedbackSessionToRecycleBin(fs.getFeedbackSessionName(), course.getId());

        RestoreFeedbackSessionAction restoreFeedbackSessionAction = getAction(params);
        JsonResult result = getJsonResult(restoreFeedbackSessionAction);
        FeedbackSessionData feedbackSessionData = (FeedbackSessionData) result.getOutput();

        assertEquals(feedbackSessionData.getFeedbackSessionName(), fs.getFeedbackSessionName());
        assertNull(feedbackSessionData.getDeletedAtTimestamp());
    }

    @Test
    @Override
    protected void testAccessControl() throws Exception {
        CourseAttributes course = typicalBundle.courses.get("typicalCourse1");
        FeedbackSessionAttributes fs = typicalBundle.feedbackSessions.get("session1InCourse1");

        String[] params = new String[] {
                Const.ParamsNames.COURSE_ID, course.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fs.getFeedbackSessionName(),
        };

        logic.moveFeedbackSessionToRecycleBin(fs.getFeedbackSessionName(), course.getId());

        verifyOnlyInstructorsOfTheSameCourseCanAccess(params);
        verifyInaccessibleWithoutModifySessionPrivilege(params);
    }
}
