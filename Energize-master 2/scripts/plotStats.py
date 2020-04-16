from argparse import ArgumentParser, ArgumentTypeError, FileType
from pylab import *

def DatabaseType( dbfile ):
	import sqlite3
	import os.path
	if os.path.exists( dbfile ):
		dbConnection = sqlite3.connect( dbfile )
		return dbConnection
	else:
		raise ArgumentTypeError( 'The supplied database does not exists.' )

if __name__ == '__main__':
	# setup the argument parser and do the parsing
	argumentParser = ArgumentParser( description = 'Tool for testing different estimation methods on real datasets.', epilog = 'This tool was written for optimizing the estimations of Energize. Copyright (c) 2012 by Tim Huetz. All rights reserved.' )
	argumentParser.add_argument( 'database', type = DatabaseType, help = 'define the database to use for doing the analysis' )
	parsedArguments = argumentParser.parse_args()

	# check if all required arguments were passed to the application
	if parsedArguments.database == None:
		argumentParser.print_help()
		exit( -1 )

	#
	xDischarging = []
	yDischarging = []
	xCharging = []
	yCharging = []

	# gather the information stored in the database
	c = parsedArguments.database.cursor()
	for row in c.execute( 'SELECT eventTime, chargingLevel, chargingState FROM rawBatteryStats ORDER BY eventTime;' ):
		if int( row[ 2 ] ) == 0:
			xDischarging.append( int( row[ 0 ] ) )
			yDischarging.append( int( row[ 1 ] ) )
		else:
			xCharging.append( int( row[ 0 ] ) )
			yCharging.append( int( row[ 1 ] ) )

	# close the database connection again
	c.close()
	parsedArguments.database.close()

	# plot the obtained dataset
	l = plot( xDischarging, yDischarging, 'r.', xCharging, yCharging, 'g.' )
	show()
