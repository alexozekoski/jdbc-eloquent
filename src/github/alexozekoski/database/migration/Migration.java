/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.migration;

import github.alexozekoski.database.Database;
import github.alexozekoski.database.model.Model;
import java.util.List;

/**
 *
 * @author alexo
 */
public abstract class Migration {

    public static boolean migrate(Database database, List<Class<? extends Model>> models) {
        for (Class<? extends Model> classe : models) {
            if (database.migrate(classe).create()) {
                return false;
            }
        }
        return true;
    }

    public static boolean fresh(Database database, List<Class<? extends Model>> models) {
        for (int i = models.size() - 1; i >= 0; i--) {
            Class<? extends Model> classe = models.get(i);
            database.migrate(classe).dropTable();
        }
        return migrate(database, models);
    }

    public static void drop(Database database, List<Class<? extends Model>> models) {
        for (Class<? extends Model> classe : models) {
            database.migrate(classe).dropTable();
        }
    }
}
