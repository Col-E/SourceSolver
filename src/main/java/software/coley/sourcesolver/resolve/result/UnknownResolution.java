package software.coley.sourcesolver.resolve.result;

non-sealed public interface UnknownResolution extends Resolution {
	UnknownResolution INSTANCE = new UnknownResolution() {};
}