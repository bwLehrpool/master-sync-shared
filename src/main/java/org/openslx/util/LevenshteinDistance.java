package org.openslx.util;

public final class LevenshteinDistance
{
	private final int insertionCost;
	private final int deletionCost;
	private final int substitutionCost;

	public LevenshteinDistance()
	{
		this( 1, 1, 1 );
	}

	public LevenshteinDistance( int insertionCost, int deletionCost, int substitutionCost )
	{
		this.validateCostArgument( insertionCost >= 0, "Insertion cost must be greater than or equal to 0" );
		this.validateCostArgument( deletionCost >= 0, "Deletion cost must be greater than or equal to 0" );
		this.validateCostArgument( substitutionCost >= 0, "Substitution cost must be greater than or equal to 0" );

		this.insertionCost = insertionCost;
		this.deletionCost = deletionCost;
		this.substitutionCost = substitutionCost;
	}

	private void validateCostArgument( boolean condition, String errorMsg )
	{
		if ( !condition ) {
			throw new IllegalArgumentException( errorMsg );
		}
	}

	public int calculateDistance( CharSequence source, CharSequence target )
	{
		if ( source == null || target == null ) {
			throw new IllegalArgumentException( "Source or target cannot be null" );
		}

		int sourceLength = source.length();
		int targetLength = target.length();

		int[][] matrix = new int[ sourceLength + 1 ][ targetLength + 1 ];
		matrix[0][0] = 0;

		for ( int row = 1; row <= sourceLength; ++row ) {
			matrix[row][0] = row;
		}

		for ( int col = 1; col <= targetLength; ++col ) {
			matrix[0][col] = col;
		}

		for ( int row = 1; row <= sourceLength; ++row ) {
			for ( int col = 1; col <= targetLength; ++col ) {
				matrix[row][col] = calcMinCost( source, target, matrix, row, col );
			}
		}

		return matrix[sourceLength][targetLength];
	}

	private int calcMinCost( CharSequence source, CharSequence target, int[][] matrix, int row, int col )
	{
		return Math.min( calcSubstitutionCost( source, target, matrix, row, col ),
				Math.min( calcDeletionCost( matrix, row, col ), calcInsertionCost( matrix, row, col ) ) );
	}

	private int calcInsertionCost( int[][] matrix, int row, int col )
	{
		return matrix[row][col - 1] + insertionCost;
	}

	private int calcDeletionCost( int[][] matrix, int row, int col )
	{
		return matrix[row - 1][col] + deletionCost;
	}

	private int calcSubstitutionCost( CharSequence source, CharSequence target, int[][] matrix, int row, int col )
	{
		int cost = 0;
		if ( source.charAt( row - 1 ) != target.charAt( col - 1 ) ) {
			cost = substitutionCost;
		}
		return matrix[row - 1][col - 1] + cost;
	}
}
