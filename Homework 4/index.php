<?php

// make sure browsers see this page as utf-8 encoded HTML
header('Content-Type: text/html; charset=utf-8');
header("Access-Control-Allow-Origin: *");

$limit = 10;
$query = isset($_REQUEST['q']) ? $_REQUEST['q'] : false;
$results = false;

if ($query) {
	// The Apache Solr Client library should be on the include path
	// which is usually most easily accomplished by placing in the
	// same directory as this script ( . or current directory is a default
	// php include path entry in the php.ini)
	require_once('Apache/Solr/Service.php');

	// create a new solr service instance - host, port, and webapp
	// path (all defaults in this example)
	$solr = new Apache_Solr_Service('localhost', 8983, '/solr/myexample');

	// if magic quotes is enabled then stripslashes will be needed
	// if (get_magic_quotes_gpc() == 1) {
	// 	$query = stripslashes($query);
	// }

	// in production code you'll always want to use a try /catch for any
	// possible exceptions emitted  by searchingexternal_pageRankFile (i.e. connection
	// problems or a query parsing error)
	try {
		if ($_GET['ranking'] == "lucene") {
			$results = $solr->search($query, 0, $limit);
		} else {
			$additionalParameters = array('sort' => 'pageRankFile desc');
			$results = $solr->search($query, 0, $limit, $additionalParameters);
		}
	} catch (Exception $e) {
		// in production you'd probably log or email this error to an admin
		// and then show a special message to the user but for this example
		// we're going to show the full exception
		die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
	}
}

?>
<html>

<head>
	<?php
	if ($results)
		echo "<title>Results: $query</title>";
	else
		echo "<title>Solr Exercise</title>";
	?>
	<style>
		.result {
			font-family: serif
		}
	</style>
</head>

<body style="font-family: sans-serif; padding: 0 2.5vw">
	<div style="display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 2.5vh auto;">
		<a style="text-decoration: none; color: black" href="/">
			<h1>Solr Exercise</h1>
		</a>
	</div>
	<form style="display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 2.5vh 2vw" accept-charset="utf-8" method="get">
		<div style="display: flex; flex-direction: row; align-items: center; justify-content: center; gap: 2vw;">
			<label for="q">Search:</label>
			<input style="width: 20vw" d="q" name="q" type="text" value="<?php echo htmlspecialchars($query, ENT_QUOTES, 'utf-8'); ?>" />
			<input type="submit" />
		</div>
		<div style="display: flex; flex-direction: row; align-items: center; justify-content: center; gap: 2vw; margin-top: 2vh;">
			<label>
				<input type="radio" checked name="ranking" value="lucene" <?php
																																	if (isset($_REQUEST['ranking']) && $_REQUEST['ranking'] == 'lucene') {
																																		echo 'checked="checked"';
																																	} ?>>
				Lucene
			</label>
			<label>
				<input type="radio" name="ranking" value="pagerank" <?php if (isset($_REQUEST['ranking']) && $_REQUEST['ranking'] == 'pagerank') {
																															echo 'checked="checked"';
																														} ?>>
				PageRank
			</label>
		</div>
	</form>

	<?php

	// display results
	if ($results) {
		$total = (int) $results->response->numFound;
		$start = min(1, $total);
		$end = min($limit, $total);
	?>
		<div>Results <?php echo $start; ?> - <?php echo $end; ?> of <?php echo $total; ?>:</div>
		<ol>
			<?php
			// iterate result documents
			$dataFolder = "/Users/syjack1997/Downloads/";
			$urlMap = array_map('str_getcsv', file($dataFolder . 'URLtoHTML_latimes_news.csv'));

			foreach ($results->response->docs as $doc) {
				$title = $doc->title;
				$url = $doc->og_url;
				$id = $doc->id;
				$description = $doc->og_description;

				if ($title == "" || $title == null) {
					$title = "N/A";
				}

				if ($url == "" || $url == null) {
					foreach ($urlMap as $row) {
						$crawledFile = $dataFolder . "latimes/" . $row[0];
						if ($id == $crawledFile) {
							$url = $row[1];
							unset($row);
							break;
						}
					}
				}

				if ($description == "" || $description == null) {
					$description = "N/A";
				}
			?>
				<li>
					<div style="border: 1px solid black; padding: 1.5vh 1vw; margin-top:-1px">
						<div>Title: <span class="result"><?php echo "<a href = '$url' target='_blank'>$title</a>" ?></span></div>
						<div>URL: <span class="result"><?php echo "<a href='$url' target='_blank'>$url</a>"; ?></span></div>
						<div>ID: <span class="result"><?php echo $id; ?></span></div>
						<div>Description: <span class="result"><?php echo $description; ?></span></div>
					</div>
				</li>
			<?php } ?>
		<?php } ?>
		</ol>
</body>

</html>