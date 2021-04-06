package socialnetwork.domain.validators;

import socialnetwork.domain.mainDom.Relation;

public class RelationValidator implements Validator<Relation> {
    @Override
    public void validate(Relation entity) throws ValidationException {
        String errors = "";

        if (entity == null)
            throw new ValidationException("entity must be not null");

        if (entity.getId().getLeft() < 0)
            errors += "Id must be a positive number!    ";

        if (entity.getId().getLeft() < 0)
            errors += "Id must be a positive number!    ";

        if (entity.getId().getLeft().equals(entity.getId().getRight()))
            errors += "A relationship must be establish between 2 users   ";

        if (!errors.equals(""))
            throw new ValidationException(errors);

    }
}
