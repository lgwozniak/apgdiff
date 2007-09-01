/*
 * $Id$
 */
package cz.startnet.utils.pgdiff;

import cz.startnet.utils.pgdiff.schema.PgConstraint;
import cz.startnet.utils.pgdiff.schema.PgSchema;
import cz.startnet.utils.pgdiff.schema.PgTable;

import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;


/**
 * Diffs constraints.
 *
 * @author fordfrog
 * @version $Id$
 */
public class PgDiffConstraints {
    /**
     * Creates a new instance of PgDiffConstraints.
     */
    private PgDiffConstraints() {
        super();
    }

    /**
     * Outputs commands for differences in constraints.
     *
     * @param writer writer the output should be written to
     * @param arguments object containing arguments settings
     * @param oldSchema original schema
     * @param newSchema new schema
     * @param primaryKey determines whether primery keys should be processed or
     *        any other constraints should be processed
     */
    public static void diffConstraints(
        final PrintWriter writer,
        final PgDiffArguments arguments,
        final PgSchema oldSchema,
        final PgSchema newSchema,
        final boolean primaryKey) {
        for (PgTable newTable : newSchema.getTables()) {
            final PgTable oldTable;

            if (oldSchema == null) {
                oldTable = null;
            } else {
                oldTable = oldSchema.getTable(newTable.getName());
            }

            // Drop constraints that no more exist or are modified
            for (PgConstraint constraint : getDropConstraints(
                        oldTable,
                        newTable,
                        primaryKey)) {
                writer.println();
                writer.println(constraint.getDropSQL(arguments.isQuoteNames()));
            }

            // Add new constraints
            for (PgConstraint constraint : getNewConstraints(
                        oldTable,
                        newTable,
                        primaryKey)) {
                writer.println();
                writer.println(
                        constraint.getCreationSQL(arguments.isQuoteNames()));
            }
        }
    }

    /**
     * Returns list of constraints that should be dropped.
     *
     * @param oldTable original table or null
     * @param newTable new table or null
     * @param primaryKey determines whether primery keys should be processed or
     *        any other constraints should be processed
     *
     * @return list of constraints that should be dropped
     *
     * @todo Constraints that are depending on a removed field should not be
     *       added to drop because they are already removed.
     */
    private static List<PgConstraint> getDropConstraints(
        final PgTable oldTable,
        final PgTable newTable,
        final boolean primaryKey) {
        final List<PgConstraint> list = new ArrayList<PgConstraint>();

        if ((newTable != null) && (oldTable != null)) {
            for (final PgConstraint constraint : oldTable.getConstraints()) {
                if (
                    (constraint.isPrimaryKeyConstraint() == primaryKey)
                        && (!newTable.containsConstraint(constraint.getName())
                        || !newTable.getConstraint(constraint.getName()).equals(
                                constraint))) {
                    list.add(constraint);
                }
            }
        }

        return list;
    }

    /**
     * Returns list of constraints that should be added.
     *
     * @param oldTable original table
     * @param newTable new table
     * @param primaryKey determines whether primery keys should be processed or
     *        any other constraints should be processed
     *
     * @return list of constraints that should be added
     */
    private static List<PgConstraint> getNewConstraints(
        final PgTable oldTable,
        final PgTable newTable,
        final boolean primaryKey) {
        final List<PgConstraint> list = new ArrayList<PgConstraint>();

        if (newTable != null) {
            if (oldTable == null) {
                for (final PgConstraint constraint : newTable.getConstraints()) {
                    if (constraint.isPrimaryKeyConstraint() == primaryKey) {
                        list.add(constraint);
                    }
                }
            } else {
                for (final PgConstraint constraint : newTable.getConstraints()) {
                    if (
                        (constraint.isPrimaryKeyConstraint() == primaryKey)
                            && (!oldTable.containsConstraint(
                                    constraint.getName())
                            || !oldTable.getConstraint(constraint.getName()).equals(
                                    constraint))) {
                        list.add(constraint);
                    }
                }
            }
        }

        return list;
    }
}
