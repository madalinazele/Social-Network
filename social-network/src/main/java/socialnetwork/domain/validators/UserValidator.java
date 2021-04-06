package socialnetwork.domain.validators;

import socialnetwork.domain.mainDom.User;

public class UserValidator implements Validator<User> {
    @Override
    public void validate(User entity) throws ValidationException {
        String errors = "";

        if ( entity == null)
            throw new ValidationException("entity must be not null");

        if ( !entity.getFirstName().matches("^[a-z A-Z]+$"))
            errors += "Invalid first name!  ";

        if ( !entity.getLastName().matches("^[a-z A-Z]+$"))
            errors += "Invalid last name!   ";

        if ( !entity.getEmail().matches("^[a-zA-Z.]+@[a-zA-Z.]+.[a-zA-Z]+$"))
            errors += "Invalid email!   ";

        if ( entity.getUsername().equals(""))
            errors += "Username can't be null  ";

        if ( entity.getUsername().equals(""))
            errors += "Password can't be null  ";

        if(!errors.equals(""))
            throw new ValidationException(errors);
    }
}
