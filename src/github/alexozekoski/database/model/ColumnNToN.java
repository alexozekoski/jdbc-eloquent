/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.model;

/**
 *
 * @author alexo
 */
public @interface ColumnNToN {

    public String table();

    public String localKey();
    
    public String localForeignKey();

    public String foreignKey();
    
    public String key();
}
