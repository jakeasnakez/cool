package net.praqma.clearcase.api;

import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.util.execute.CmdResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public class Describe extends Command<List<String>> {

    private static Logger logger = Logger.getLogger( Describe.class.getName() );

    private String itemSeparator = "\n";

    public enum Type {
        SHORT( "-short" ),
        LONG( "-long" ),
        FMT( "-fmt" );

        private String text;

        private Type( String text ) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private Type type;

    public static final Property memberOfClosure = new Property( "member_of_closure" );
    public static final Property dependsOn = new Property( "depends_on" );
    public static final Property dependsOnClosure = new Property( "depends_on_closure" );

    public static class Property {

        private String name;
        private String type = "p";

        private boolean commaSeparated = false;

        public Property( String name ) {
            this.name = name;
        }

        public Property commaSeparate() {
            commaSeparated = true;

            return this;
        }

        public Property commaSeparate( boolean commaSeparated ) {
            this.commaSeparated = commaSeparated;

            return this;
        }

        public boolean isCommaSeparated() {
            return commaSeparated;
        }

        public Property setType( String type ) {
            this.type = type;

            return this;
        }

        public String getModifiers() {
            StringBuilder b = new StringBuilder(  );

            if( commaSeparated ) {
                b.append( "C" );
            }

            return b.toString();
        }

        @Override
        public String toString() {
            return "%[" + this.name + "]" + getModifiers() + type;
        }

        @Override
        public Property clone() {
            Property p = new Property( name );
            p.type = type;
            p.commaSeparated = commaSeparated;

            return p;
        }
    }

    private List<Property> properties = new ArrayList<Property>(  );

    private String objectSelector;

    public Describe( String objectSelector ) {
        this.objectSelector = objectSelector;
    }

    public Describe( UCMEntity entity ) {
        this.objectSelector = entity.getFullyQualifiedName();
    }

    public Describe addModifier( Property property ) {
        properties.add( property );
        type = Type.FMT;

        return this;
    }

    public Describe addModifier( Property property, boolean commaSeparate ) {
        properties.add( property.clone().commaSeparate( commaSeparate ) );
        type = Type.FMT;

        return this;
    }

    @Override
    public List<String> execute() throws CleartoolException {
        CmdResult result = runCommand();

        return result.stdoutList;
    }

    /**
     * This method will return a grouped result by property for easier access.
     */
    public Map<String, String[]> describe() throws CleartoolException {
        CmdResult result = runCommand();
        return getResults( getElements( result.stdoutBuffer.toString() ) );
    }

    @Override
    public String getCommand() {
        return "describe";
    }

    @Override
    public String getCommandLine() {
        StringBuilder b = new StringBuilder(  );
        b.append( "describe " ).append( type ).append( " " );

        int i = 0;
        for( ; i < properties.size() - 1 ; ++i ) {
            b.append( properties.get( i ) ).append( itemSeparator );
        }

        b.append( properties.get( i ) );

        b.append( " " ).append( objectSelector );

        return b.toString();
    }


    public Map<String, String[]> getResults( List<String> elements ) {
        logger.finest( "Get results for " + elements );

        if( elements.size() != properties.size() ) {
            throw new IllegalStateException( "The number of elements and the number of describers does not match" );
        }

        Map<String, String[]> results = new HashMap<String, String[]>(  );

        for( int i = 0 ; i < properties.size() ; ++i ) {
            String[] r;
            if( properties.get( i ).isCommaSeparated() ) {
                r = elements.get( i ).trim().split( "\\s*,\\s*" );
            } else {
                r = elements.get( i ).trim().split( "\\s+" );
            }

            results.put( properties.get( i ).name, r );
        }

        return results;
    }

    public List<String> getElements( String string ) {
        logger.finest( "Get elements for " + string );

        String[] lines = string.split( itemSeparator );
        List<String> elements = new ArrayList<String>(  );
        for( String line : lines ) {
            elements.add( line );
        }

        return elements;
    }
}
