package emailtool.api;

import emailtool.exception.InvalidExcelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(InvalidExcelException.class)
    public ProblemDetail handleInvalidExcel(
            InvalidExcelException exception
    ) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleOversizedUpload(
            MaxUploadSizeExceededException exception
    ) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "The file must be no larger than 5 MB, "
                        + "and the complete request must not exceed 6 MB."
        );
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ProblemDetail handleMissingFile(
            MissingServletRequestPartException exception
    ) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Upload the Excel file using the form field named file."
        );
    }

    @ExceptionHandler(MultipartException.class)
    public ProblemDetail handleMultipart(
            MultipartException exception
    ) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Cannot read the multipart upload."
        );
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ProblemDetail handleFrameworkError(
            ErrorResponseException exception
    ) {
        return exception.getBody();
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedError(Exception exception) {
        log.error(
                "Unexpected email processing failure: {}",
                exception.getClass().getSimpleName()
        );

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "The file could not be processed because of a server error."
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleMissingResource(
            NoResourceFoundException exception
    ) {
        log.warn(
                "Requested resource not found: {}",
                exception.getResourcePath()
        );

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "The requested page or resource was not found."
        );
    }
}
