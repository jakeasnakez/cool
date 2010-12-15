package net.praqma.clearcase.ucm.entities;


import net.praqma.clearcase.ucm.persistence.UCMContext;
import net.praqma.clearcase.ucm.persistence.UCMStrategyCleartool;
import net.praqma.clearcase.ucm.persistence.UCMStrategyXML;
import net.praqma.util.Debug;

public abstract class UCM
{

	
	/* Make sure, that we're using the same instance of the context! */
	public static UCMContext context = null;
	
	public enum ContextType
	{
		XML,
		CLEARTOOL
	}
	
	public static void SetContext( ContextType ct )
	{
		if( context != null )
		{
			logger.warning( "Context is already set" );
			return;
		}
		
		logger.log( "Setting context type to " + ct.toString() );
		
		switch( ct )
		{
		case XML:
			context = new UCMContext( new UCMStrategyXML() );
			break;
			
		default:
			context = new UCMContext( new UCMStrategyCleartool() );
		}
	}
	
	protected static Debug logger = Debug.GetLogger( false );
	
	protected static final String filesep = System.getProperty( "file.separator" );
	protected static final String linesep = System.getProperty( "line.separator" );
	public static final String delim      = "::";
}