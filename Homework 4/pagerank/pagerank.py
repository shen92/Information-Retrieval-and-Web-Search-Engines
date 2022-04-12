import networkx as nx

graph = nx.read_edgelist("edgelist.txt", create_using=nx.DiGraph())
page_rank = nx.pagerank(graph, alpha=0.85, personalization=None, max_iter=30, tol=1e-06, nstart=None, weight='weight', dangling=None)

src_folder = "/Users/syjack1997/Downloads/latimes/" # path to crawled html files

with open("external_pageRankFile.txt", "w") as output_file:
    for k, v in page_rank.items():
        output_file.write(src_folder + k + "=" + str(v) + "\n")