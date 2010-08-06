import java.io.BufferedOutputStream;
import java.io.PrintWriter;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
	private final DiffFormatter diffFmt = new DiffFormatter( //
			new BufferedOutputStream(System.out));

	@Option(name = "--", metaVar = "metaVar_paths", multiValued = true, handler = PathTreeFilterHandler.class)
	// BEGIN -- Options shared with Log
	@Option(name = "-p", usage = "usage_showPatch")
	boolean showPatch;

	@Option(name = "-M", usage = "usage_detectRenames")
	private boolean detectRenames;

	@Option(name = "-l", usage = "usage_renameLimit")
	private Integer renameLimit;

	@Option(name = "--name-status", usage = "usage_nameStatus")
	private boolean showNameAndStatusOnly;

	void ignoreSpaceAtEol(@SuppressWarnings("unused") boolean on) {
		diffFmt.setRawTextFactory(RawTextIgnoreTrailingWhitespace.FACTORY);
	}
	void ignoreLeadingSpace(@SuppressWarnings("unused") boolean on) {
		diffFmt.setRawTextFactory(RawTextIgnoreLeadingWhitespace.FACTORY);
	}
	void ignoreSpaceChange(@SuppressWarnings("unused") boolean on) {
		diffFmt.setRawTextFactory(RawTextIgnoreWhitespaceChange.FACTORY);
	}
	void ignoreAllSpace(@SuppressWarnings("unused") boolean on) {
		diffFmt.setRawTextFactory(RawTextIgnoreAllWhitespace.FACTORY);
	}

	@Option(name = "-U", aliases = { "--unified" }, metaVar = "metaVar_linesOfContext")
	void unified(int lines) {
		diffFmt.setContext(lines);
	}

	@Option(name = "--abbrev", metaVar = "n")
	void abbrev(int lines) {
		diffFmt.setAbbreviationLength(lines);
	}
	@Option(name = "--full-index")
	void abbrev(@SuppressWarnings("unused") boolean on) {
		diffFmt.setAbbreviationLength(Constants.OBJECT_ID_STRING_LENGTH);
	}

	// END -- Options shared with Log
		List<DiffEntry> files = scan();

		if (showNameAndStatusOnly) {
			nameStatus(out, files);
			out.flush();

		} else {
			diffFmt.setRepository(db);
			diffFmt.format(files);
			diffFmt.flush();
		}
	}

	static void nameStatus(PrintWriter out, List<DiffEntry> files) {
		for (DiffEntry ent : files) {
			switch (ent.getChangeType()) {
			case ADD:
				out.println("A\t" + ent.getNewPath());
				break;
			case DELETE:
				out.println("D\t" + ent.getOldPath());
				break;
			case MODIFY:
				out.println("M\t" + ent.getNewPath());
				break;
			case COPY:
				out.format("C%1$03d\t%2$s\t%3$s", ent.getScore(), //
						ent.getOldPath(), ent.getNewPath());
				out.println();
				break;
			case RENAME:
				out.format("R%1$03d\t%2$s\t%3$s", ent.getScore(), //
						ent.getOldPath(), ent.getNewPath());
				out.println();
				break;
			}
		}
	}

	private List<DiffEntry> scan() throws IOException {
		List<DiffEntry> files = DiffEntry.scan(walk);
		if (detectRenames) {
			RenameDetector rd = new RenameDetector(db);
			if (renameLimit != null)
				rd.setRenameLimit(renameLimit.intValue());
			rd.addAll(files);
			files = rd.compute(new TextProgressMonitor());
		return files;