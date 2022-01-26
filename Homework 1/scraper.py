import json
from bs4 import BeautifulSoup
import time
from time import sleep
import requests
from random import randint
from html.parser import HTMLParser

USER_AGENT = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) '
                  'Chrome/61.0.3163.100 Safari/537.36'}
BASE_URL = 'http://www.ask.com/web?q='

class SearchEngine:
    @staticmethod
    def search(query, sleep=True):
        if sleep:  # Prevents loading too many pages too soon
            time.sleep(randint(5, 10))
        query_string = '+'.join(query.split())  # for adding + between words for the query
        query_url = BASE_URL + query_string
        soup = BeautifulSoup(requests.get(query_url, headers=USER_AGENT).text, "html.parser")
        results = SearchEngine.scrape_search_result(soup, query_string)
        return results

    @staticmethod
    def scrape_search_result(soup, query_string):
        raw_results = soup.find_all("div", attrs={"class": "PartialSearchResults-item-title"})
        results_set = set()
        results_list = []
        # implement a check to get only 10 results and also check that URLs must not be duplicated
        for result in raw_results:
            link = result.find('a').get('href')
            if link not in results_set:
                results_set.add(link)
                results_list.append(link)
        
        if len(results_list) < 10:
            query_url_2 = BASE_URL + query_string + "&page=2"
            soup_2 = BeautifulSoup(requests.get(query_url_2, headers=USER_AGENT).text, "html.parser")
            raw_results_2 = soup_2.find_all("div", attrs={"class": "PartialSearchResults-item-title"})

            for result in raw_results_2:
                link = result.find('a').get('href')
                if link not in results_set:
                    results_set.add(link)
                    results_list.append(link)

        return results_list[:10]

def format_url(url):
    # remove http/https
    if "https" in url:
        new_url = url[5:]
    elif "http" in url:
        new_url = url[4:]
    else:
        new_url = url
    
    # remove end slash
    if new_url[len(new_url) - 1] == '/':
        return new_url[:len(new_url) - 1]
    
    return new_url

ask_query_file = open("100QueriesSet3.txt", "r")

ask_query_list = []
ask_results_dict = {}

for line in ask_query_file:
    query = line.strip()
    results = SearchEngine.search(query)
    # print(len(results), query)
    ask_results_dict[query] = results
    ask_query_list.append(query)

ask_results_json = json.dumps(ask_results_dict, indent=4)

with open("hw1.json", "w") as hw1_json_file:
    hw1_json_file.write(ask_results_json)

google_results_file = open("Google_Result3.json", "r")
google_results = json.load(google_results_file)

# index: index of auery, value: num overlap of query
overlap_count_list = []
# query: [(ask_rank, google_rank)]
overlap_pair_dict = {}
# index: index of auery, value: spearmans coefficient of query
spearmans_coefficient_list = []

for query in ask_query_list:
    overlap_pair_dict[query] = []
    num_overlap = 0

    ask_rank = 1
    for raw_ask_url in ask_results_dict[query]:
        ask_url = format_url(raw_ask_url)

        google_rank = 1
        for raw_google_url in google_results[query]:
            google_url = format_url(raw_google_url)

            if ask_url.lower() == google_url.lower():
                num_overlap += 1
                if query not in overlap_pair_dict:
                    overlap_pair_dict[query] = (ask_rank, google_rank)
                else:
                    overlap_pair_dict[query].append((ask_rank, google_rank))
                break

            google_rank += 1

        ask_rank += 1

    overlap_count_list.append(num_overlap)

query_index = 0
for query in ask_query_list:
    
    num_overlaps = overlap_count_list[query_index]

    if num_overlaps == 0:
        spearmans_coefficient = 0
    
    elif num_overlaps == 1:
        for overlap_pair in overlap_pair_dict[query]:
            if overlap_pair[0] == overlap_pair[1]:
                spearmans_coefficient = 1
            else:
                spearmans_coefficient = 0
    
    else:
        total_difference = 0
        for overlap_pair in overlap_pair_dict[query]:
            total_difference += (overlap_pair[1] - overlap_pair[0]) ** 2
        spearmans_coefficient = 1 - (6 * total_difference) / (num_overlaps * ((num_overlaps ** 2) - 1))

    spearmans_coefficient_list.append(spearmans_coefficient)

    query_index += 1

with open("hw1.csv", "w") as hw1_csv_file:
    total_overlap = 0
    total_overlap_percentage = 0
    total_spearman = 0

    hw1_csv_file.write("Queries, Number of Overlapping Results, Percent Overlap, Spearman Coefficient\n")
    
    for i in range(100):
        hw1_csv_file.write("Query " + \
            str((i + 1)) + ", " + \
            str(float(overlap_count_list[i])) + ", " + \
            str(float(overlap_count_list[i]*10)) + ", " + \
            str(float(spearmans_coefficient_list[i])) + "\n")
        
        total_overlap += overlap_count_list[i]
        total_overlap_percentage += float(overlap_count_list[i]*10)
        total_spearman += spearmans_coefficient_list[i]

    summary = "Averages, " + \
        str(float(total_overlap / 100)) + ", " + \
        str(float(total_overlap_percentage/100)) + ", " + \
        str(float(total_spearman / 100))
    
    hw1_csv_file.write(summary)

