package socialnetwork.domain.validators;

import socialnetwork.domain.dto.MessageDTO;

public class MessageValidator implements Validator<MessageDTO> {
    @Override
    public void validate(MessageDTO entity) throws ValidationException {
        String errors = "";

        if (entity.getMessage().equals(""))
            errors += "Message can not be null";

        if (!errors.equals(""))
            throw new ValidationException(errors);
    }
}
