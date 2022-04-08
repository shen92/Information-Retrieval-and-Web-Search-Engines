import networkx as nx

graph = nx.read_edgelist("edgelist.txt")
page_rank = nx.pagerank(graph)

src_folder = "/Users/syjack1997/Downloads/"

with open("external_PageRankFile.txt", "w") as output_file:
    for k, v in page_rank.items():
        output_file.write(src_folder + k + "=" + str(v) + "\n")